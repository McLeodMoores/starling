/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited, All rights reserved.
 * based on code Copyright (C) 2009 - present OpenGamma Limited.
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.starling.client.utils;

import com.google.common.collect.Lists;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.fudgemsg.ServletContextHolder;
import org.fudgemsg.AnnotationReflector;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to save reflections fudge scan.
 */
public class ReflectionsSaver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionsSaver.class);

  public static Reflections init() {
    // hack to handle non-existent classpath directory entries
    List<Vfs.UrlType> urlTypes = Lists.newArrayList(Vfs.getDefaultUrlTypes());
    urlTypes.add(0, new OGFileUrlType());
    urlTypes.add(1, new OGJNDIUrlType());
    Vfs.setDefaultURLTypes(urlTypes);

    // init annotation reflector, which needs this class loader
    Set<ClassLoader> loaders = new HashSet<>();
    loaders.add(OpenGammaFudgeContext.class.getClassLoader());
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader != null) {
        loaders.add(loader);
      }
    } catch (Exception ex) {
      // ignore
    }
    Collection<URL> urls;
    if (ServletContextHolder.getContext() == null) {
      urls = ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath());
    } else {
      urls = ClasspathHelper.forManifest(ClasspathHelper.forWebInfLib(ServletContextHolder.getContext()));
      loaders.add(ServletContextHolder.getContext().getClassLoader());
    }
    Configuration config = new ConfigurationBuilder()
        .setUrls(urls)
        .setScanners(new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .addClassLoaders(loaders)
        .useParallelExecutor();
    AnnotationReflector.initDefaultReflector(new AnnotationReflector(config));
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    return reflector.getReflector();
  }

  //-------------------------------------------------------------------------
  // handle non-existent classpath directory entries
  private static final class OGFileUrlType implements Vfs.UrlType {

    @Override
    public boolean matches(URL url) throws Exception {
      return url.getProtocol().equals("file") && !url.toExternalForm().contains(".jar");
    }

    @Override
    public Vfs.Dir createDir(URL url) throws Exception {
      File file = Vfs.getFile(url);
      if (file == null || file.exists() == false) {
        LOGGER.warn("URL could not be resolved to a file: " + url);
        return new EmptyDir(file);
      } else {
        return new SystemDir(file);
      }
    }

    @Override
    public String toString() {
      return "directories (OGFileUrlType fix)";
    }
  }

  private static final class OGJNDIUrlType implements Vfs.UrlType {

    @Override
    public boolean matches(URL url) throws Exception {
      return url.getProtocol().equals("jndi") && !url.toExternalForm().contains(".jar");
    }

    @Override
    public Vfs.Dir createDir(URL url) throws Exception {
      File file = Vfs.getFile(url);
      if (file == null || file.exists() == false) {
        LOGGER.warn("URL could not be resolved to a file: " + url);
        return new EmptyDir(file);
      } else {
        return new SystemDir(file);
      }
    }

    @Override
    public String toString() {
      return "directories (OGJNDIUrlType fix)";
    }

  }

  //-------------------------------------------------------------------------
  // handle non-existent classpath directory entries
  private static final class EmptyDir implements Vfs.Dir {
    private final File _file;

    private EmptyDir(File file) {
      this._file = file;
    }

    public String getPath() {
      return _file.getPath().replace("\\", "/");
    }


    public Iterable<Vfs.File> getFiles() {
      return Collections.emptyList();  // just return no files
    }

    public void close() {
    }

    @Override
    public String toString() {
      return _file.toString();
    }
  }
}
