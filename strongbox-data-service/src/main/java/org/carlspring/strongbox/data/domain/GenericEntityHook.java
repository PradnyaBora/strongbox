package org.carlspring.strongbox.data.domain;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.hook.ORecordHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * @author Sergey Bespalov
 *
 */
public class GenericEntityHook extends ORecordHookAbstract
{

    private static final Logger logger = LoggerFactory.getLogger(GenericEntityHook.class);

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode()
    {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

    @Override
    public RESULT onRecordBeforeCreate(ORecord iRecord)
    {
        RESULT result = RESULT.RECORD_NOT_CHANGED;
        if (!(iRecord instanceof ODocument))
        {
            return result;
        }
        ODocument doc = (ODocument) iRecord;

        String uuid = doc.field("uuid");
        if (uuid == null || uuid.trim().isEmpty())
        {
            throw new OValidationException(
                    String.format("Failed to persist document [%s]. UUID can't be empty or null.",
                                  doc.getSchemaClass()));
        }

        for (OClass oClass : doc.getSchemaClass().getAllSuperClasses())
        {
            if (!"ArtifactEntry".equals(oClass.getName()))
            {
                continue;
            }
            ODocument artifactCoordinates = doc.field("artifactCoordinates");
            String artifactCoordinatesPath = artifactCoordinates == null ? "" : artifactCoordinates.field("path");
            
            String artifactEntryPath = doc.field("artifactPath");
            artifactEntryPath = artifactEntryPath == null ? "" : artifactEntryPath.trim();
            
            if (artifactCoordinatesPath.trim().isEmpty() && artifactEntryPath.trim().isEmpty())
            {
                throw new OValidationException(
                        String.format("Failed to persist document [%s]. 'artifactPath' can't be empty or null.",
                                      doc.getSchemaClass()));
            }
            else if (artifactCoordinates != null && !artifactEntryPath.equals(artifactCoordinatesPath))
            {
                throw new OValidationException(
                        String.format("Failed to persist document [%s]. Paths [%s] and [%s] dont match.",
                                      doc.getSchemaClass(), artifactEntryPath, artifactCoordinatesPath));
            }
            
            break;
        }
        
        return result;
    }

}
