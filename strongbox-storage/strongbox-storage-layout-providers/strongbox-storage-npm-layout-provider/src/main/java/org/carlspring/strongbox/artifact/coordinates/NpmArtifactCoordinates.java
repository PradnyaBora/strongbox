package org.carlspring.strongbox.artifact.coordinates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.semver.Version;
import org.springframework.util.Assert;

/**
 * This class is an {@link ArtifactCoordinates} implementation for npm artifacts. <br>
 * See <a href="https://docs.npmjs.com/files/package.json">Official npm package specification</a>.
 * 
 * @author sbespalov
 *
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "npmArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
public class NpmArtifactCoordinates extends AbstractArtifactCoordinates
{
    public static final String NPM_NAME_REGEX = "[a-z0-9][\\w-.]*";
    public static final String NPM_EXTENSION_REGEX = "\\.tgz";
    public static final String NPM_PACKAGE_PATH_REGEX = "(@?" + NPM_NAME_REGEX + ")/(" + NPM_NAME_REGEX + ")/(.+)/"
            + NPM_NAME_REGEX + "-(.+?(?=" + NPM_EXTENSION_REGEX + "))" + NPM_EXTENSION_REGEX;

    private static final Pattern NPM_NAME_PATTERN = Pattern.compile(NPM_NAME_REGEX);
    private static final Pattern NPM_PATH_PATTERN = Pattern.compile(NPM_PACKAGE_PATH_REGEX);

    private static final String SCOPE = "scope";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    public NpmArtifactCoordinates()
    {
        defineCoordinates(SCOPE, NAME, VERSION);
    }

    public NpmArtifactCoordinates(String scope,
                                  String name,
                                  String version)
    {
        setScope(scope);
        setName(name);
        setVersion(version);
    }

    public String getScope()
    {
        return getCoordinate(SCOPE);
    }

    public void setScope(String scope)
    {
        if (scope == null)
        {
            return;
        }
        Assert.isTrue(scope.startsWith("@"), "Scope should starts with '@'.");
        setCoordinate(SCOPE, scope);
    }

    public String getName()
    {
        return getCoordinate(NAME);
    }

    public void setName(String name)
    {
        Matcher matcher = NPM_NAME_PATTERN.matcher(name);
        Assert.isTrue(matcher.matches(),
                      "The artifact's name should follow the NPM specification  (https://docs.npmjs.com/files/package.json#name).");

        setCoordinate(NAME, name);
    }

    @Override
    public String getId()
    {
        return getName();
    }

    @Override
    public void setId(String id)
    {
        setName(id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        Version.parse(version);
        setCoordinate(VERSION, version);
    }

    @Override
    public String toPath()
    {
        String scopeLocal = getScope();
        String nameLocal = getName();
        String versionLocal = getVersion();

        String group = scopeLocal == null ? nameLocal : scopeLocal;
        String packageFileName = String.format("%s-%s.tgz", nameLocal, versionLocal);

        return String.format("%s/%s/%s/%s", group, nameLocal, versionLocal, packageFileName);
    }

    public static NpmArtifactCoordinates parse(String path)
    {
        Matcher matcher = NPM_PATH_PATTERN.matcher(path);

        Assert.isTrue(matcher.matches(),
                      String.format("Illegal artifact path [%s], NPM artifact path should be in the form of '{artifactGroup}/{artifactName}/{artifactFile}'.",
                                    path));

        String group = matcher.group(1);
        String name = matcher.group(2);
        String version = matcher.group(3);

        if (group.startsWith("@"))
        {
            return new NpmArtifactCoordinates(group, name, version);
        }
        return new NpmArtifactCoordinates(null, name, version);
    }

    public static NpmArtifactCoordinates of(String name,
                                            String version)
    {
        if (name.contains("/"))
        {
            String[] nameSplit = name.split("/");
            return new NpmArtifactCoordinates(nameSplit[0], nameSplit[1], version);
        }
        return new NpmArtifactCoordinates(null, name, version);
    }

    public static NpmArtifactCoordinates of(String scope,
                                            String name,
                                            String version)
    {
        return new NpmArtifactCoordinates(scope, name, version);
    }

}
