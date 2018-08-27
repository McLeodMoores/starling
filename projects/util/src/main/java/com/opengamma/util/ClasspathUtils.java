/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;

/**
 * Classpath utilities
 */
public class ClasspathUtils {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathUtils.class);
  /**
   * The class path.
   */
  private static final ImmutableList<URI> CLASSPATH;

  static {
    CLASSPATH = ImmutableList.copyOf(forManifest(forJavaClassPath()));
  }

  //-------------------------------------------------------------------------
  private static Set<URI> forJavaClassPath() {
    final Set<URI> uris = Sets.newLinkedHashSet();
    final String javaClassPath = System.getProperty("java.class.path");
    if (javaClassPath != null) {
      for (final String path : javaClassPath.split(File.pathSeparator)) {
        try {
          uris.add(new File(path).toURI());
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    }
    return uris;
  }

  private static Set<URI> forManifest(final Iterable<URI> uris) {
    final Set<URI> result = Sets.newLinkedHashSet();
    for (final URI uri : uris) {
      result.addAll(forManifest(uri));
    }
    return result;
  }

  private static Set<URI> forManifest(final URI uri) {
    final Set<URI> result = Sets.newLinkedHashSet();
    result.add(uri);
    try {
      final String part = ClasspathHelper.cleanPath(uri.toURL());
      final File jarFile = new File(part);
      try (JarFile myJar = new JarFile(part)) {
        URI validUri = tryToGetValidUri(jarFile.getPath(), new File(part).getParent(), part);
        if (validUri != null) {
          result.add(validUri);
        }
        final Manifest manifest = myJar.getManifest();
        if (manifest != null) {
          final String classPath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
          if (classPath != null) {
            for (final String jar : classPath.split(" ")) {
              validUri = tryToGetValidUri(jarFile.getPath(), new File(part).getParent(), jar);
              if (validUri != null) {
                result.add(validUri);
              }
            }
          }
        }
      }
    } catch (final IOException e) {
      // don't do anything, we're going on the assumption it is a jar, which could be wrong
    }
    return result;
  }

  private static URI tryToGetValidUri(final String workingDir, final String path, final String filename) {
    try {
      if (new File(filename).exists()) {
        return new File(filename).toURI();
      }
      if (new File(path + File.separator + filename).exists()) {
        return new File(path + File.separator + filename).toURI();
      }
      if (new File(workingDir + File.separator + filename).exists()) {
        return new File(workingDir + File.separator + filename).toURI();
      }
      if (new File(new URL(filename).getFile()).exists()) {
        return new File(new URL(filename).getFile()).toURI();
      }
    } catch (final MalformedURLException e) {
      // don't do anything, we're going on the assumption it is a jar, which could be wrong
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains an array of URLs from an array of file names.
   *
   * @param classpath  the classpath, may be null
   * @return an array of URLs, not null
   */
  public static URL[] getClasspathURLs(final String[] classpath) {
    if (classpath == null) {
      return new URL[0];
    }
    final Set<URL> classpathUrls = new HashSet<>();
    for (final String classpathEntry : classpath) {
      final File f = new File(classpathEntry);
      if (!f.exists()) {
        LOGGER.debug("Skipping non-existent classpath entry '{}'", classpathEntry);
        continue;
      }
      try {
        classpathUrls.add(f.toURI().toURL());
      } catch (final MalformedURLException e) {
        throw new OpenGammaRuntimeException("Error interpreting classpath entry '" + classpathEntry + "' as URL", e);
      }
    }
    final URL[] classpathUrlArray = classpathUrls.toArray(new URL[0]);
    return classpathUrlArray;
  }

  public static URL[] getClasspathURLs(final Collection<String> classpath) {
    String[] classpathArray = new String[classpath.size()];
    classpathArray = classpath.toArray(classpathArray);
    return getClasspathURLs(classpathArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains the class path as URIs.
   *
   * @return the classpath, not null
   */
  public static ImmutableList<URI> getURIs() {
    return CLASSPATH;
  }

  /**
   * Obtains the class path as dependencies.
   *
   * @return the dependencies, not null
   */
  public static ImmutableList<DependencyInfo> getDependencies() {
    return DependencyInfo.DEPENDENCIES;
  }

  //-------------------------------------------------------------------------
  /**
   * Information about a dependency in the classpath.
   */
  public static class DependencyInfo {
    static final ImmutableList<DependencyInfo> DEPENDENCIES;
    private final URL _url;
    private final String _version;
    private final String _groupId;
    private final String _artifactId;

    static {
      final Builder<DependencyInfo> builder = ImmutableList.builder();
      for (final URI uri : CLASSPATH) {
        try {
          builder.add(new DependencyInfo(uri));
        } catch (final MalformedURLException ex) {
          // ignore and continue
        }
      }
      DEPENDENCIES = builder.build();
    }

    public DependencyInfo(final URI uri) throws MalformedURLException {
      _url = uri.toURL();

      final FilterBuilder filter = new FilterBuilder().include(".*pom[.]properties");
      final Reflections ref = new Reflections(new ResourcesScanner(), _url, filter);
      final Set<String> resources = ref.getResources(filter);
      final Properties properties = new Properties();
      if (resources.size() == 1) {
        final String relativePath = resources.iterator().next();
        try (URLClassLoader cl = new URLClassLoader(new URL[] {_url}, null)) {
          final URL resource = cl.getResource(relativePath);
          if (resource != null) {
            try (InputStream in = resource.openStream()) {
              properties.load(in);
            } catch (final IOException ex) {
              LOGGER.debug(ex.getMessage(), ex);
            }
          }
        } catch (final IOException ex2) {
          LOGGER.debug(ex2.getMessage(), ex2);
        }
      } else if (_url.toString().endsWith(".jar")) {
        String name = StringUtils.substringAfterLast(_url.toString(), "/");
        name = StringUtils.substringBeforeLast(name, ".jar");
        properties.setProperty("version", StringUtils.substringAfterLast(name, "-"));
        properties.setProperty("groupId", "?");
        properties.setProperty("artifactId", StringUtils.substringBeforeLast(name, "-"));
      }
      _version = properties.getProperty("version", "?");
      _groupId = properties.getProperty("groupId", "?");
      _artifactId = properties.getProperty("artifactId", "?");
    }

    /**
     * Is the dependency a Jar file.
     * @return true if a jar
     */
    public boolean isJarFile() {
      return _url.toString().endsWith(".jar");
    }

    /**
     * Is the dependency information parsed.
     * @return true if a jar
     */
    public boolean isInfoParsed() {
      return "?".equals(_version) == false;
    }

    /**
     * Is the groupId information parsed.
     * @return true if a jar
     */
    public boolean isGroupParsed() {
      return "?".equals(_groupId) == false;
    }

    /**
     * Get URL.
     * @return the URL
     */
    public URL getUrl() {
      return _url;
    }

    /**
     * Get version.
     * @return the version
     */
    public String getVersion() {
      return _version;
    }

    /**
     * Get group id.
     * @return the group id
     */
    public String getGroupId() {
      return _groupId;
    }

    /**
     * Get artifact id.
     * @return the artifact id
     */
    public String getArtifactId() {
      return _artifactId;
    }

    @Override
    public String toString() {
      return _groupId + ":" + _artifactId + ":" + _version;
    }
  }

}
