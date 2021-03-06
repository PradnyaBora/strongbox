package org.carlspring.strongbox.storage.validation.resource;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Kate Novik.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactOperationsValidatorTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactOperationsValidatorTest.class);

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/storage0/releases");

    private static final String STORAGE_ID = "storage0";

    private static final String REPOSITORY_ID = "releases";

    private static MockMultipartFile multipartFile;

    private static InputStream is;

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox" })
    public static class SpringConfig
    {
    }

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private ConfigurationManager configurationManager;


    @Before
    public void setUp()
            throws Exception
    {
        File baseDir = new File(REPOSITORY_BASEDIR + "/" + REPOSITORY_ID);
        if (!baseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }

        is = new RandomInputStream(20480000);

        multipartFile = new MockMultipartFile("artifact",
                                              "strongbox-validate-8.1.jar",
                                              "application/octet-stream",
                                              is);
    }

    @After
    public void tearDown()
            throws Exception
    {
        Repository repository = getConfiguration().getStorage(STORAGE_ID)
                                                  .getRepository(REPOSITORY_ID);
        repository.setArtifactMaxSize(0L);
    }

    @Test
    public void checkArtifactSizeTest()
            throws IOException
    {
        long size = multipartFile.getSize();

        Repository repository = getConfiguration().getStorage(STORAGE_ID)
                                                  .getRepository(REPOSITORY_ID);
        repository.setArtifactMaxSize(size + 1000L);

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        repository.setArtifactMaxSize(size - 10L);

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);

            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }

        repository.setArtifactMaxSize(0L);

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        File file = new File(REPOSITORY_BASEDIR.getAbsolutePath() + "validate-test.jar");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile()
            .mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        MockMultipartFile emptyFile = new MockMultipartFile("artifact",
                                                            "strongbox-validate-empty.jar",
                                                            "application/octet-stream",
                                                            new FileInputStream(file));

        size = emptyFile.getSize();

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, emptyFile);

            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
