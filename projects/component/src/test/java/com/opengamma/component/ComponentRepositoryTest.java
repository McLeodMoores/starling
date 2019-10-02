/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.context.Phased;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ServletContextAware;
import org.testng.annotations.Test;

import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.test.TestGroup;

/**
 * Test component repository.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentRepositoryTest {

  private static final ComponentLogger LOGGER = ComponentLogger.Sink.INSTANCE;

  /**
   *
   */
  public void testRegisterSimple() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockSimple.class, "test");
    final MockSimple mock = new MockSimple();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockSimple.class).size());
    assertEquals(mock, repo.getInstances(MockSimple.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockSimple.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockSimple.class, repo.getTypeInfo(MockSimple.class).getType());
    assertEquals(info, repo.getTypeInfo(MockSimple.class).getInfo("test"));
    assertEquals(info, repo.findInfo(MockSimple.class, "test"));
    assertEquals(info, repo.findInfo("MockSimple", "test"));
    assertEquals(info, repo.findInfo("MockSimple::test"));
    final LinkedHashMap<String, String> input = new LinkedHashMap<>();
    input.put("a", "MockSimple::test");
    input.put("b", "Rubbish::test");
    input.put("c", "MockSimple::test");
    final LinkedHashMap<String, ComponentInfo> found = repo.findInfos(input);
    assertEquals(2, found.size());
    assertEquals(info, found.get("a"));
    assertEquals(info, found.get("c"));
    assertEquals(false, found.containsKey("b"));
    repo.start();
    repo.stop();
  }

  /**
   *
   */
  public void testRegisterLifecycle() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    final MockInterfaces mock = new MockInterfaces();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
    assertEquals(0, mock._starts);
    assertEquals(0, mock._stops);
    repo.start();
    assertEquals(1, mock._starts);
    assertEquals(0, mock._stops);
    repo.stop();
    assertEquals(1, mock._starts);
    assertEquals(1, mock._stops);
  }

  /**
   *
   */
  public void testRegisterPhased() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final List<String> order = new ArrayList<>();
    /**
     *
     */
    class Simple1 implements Lifecycle {
      @Override
      public void start() {
        order.add("Simple1");
      }

      @Override
      public void stop() {
        order.add("Simple1");
      }

      @Override
      public boolean isRunning() {
        return false;
      }
    }
    /**
     *
     */
    class Simple2 implements Lifecycle {
      @Override
      public void start() {
        order.add("Simple2");
      }

      @Override
      public void stop() {
        order.add("Simple2");
      }

      @Override
      public boolean isRunning() {
        return false;
      }
    }
    /**
     *
     */
    class PhaseMinus1 implements Lifecycle, Phased {
      @Override
      public void start() {
        order.add("-1");
      }

      @Override
      public void stop() {
        order.add("-1");
      }

      @Override
      public boolean isRunning() {
        return false;
      }

      @Override
      public int getPhase() {
        return -1;
      }
    }
    /**
     *
     */
    class PhasePlus1 implements Lifecycle, Phased {
      @Override
      public void start() {
        order.add("1");
      }

      @Override
      public void stop() {
        order.add("1");
      }

      @Override
      public boolean isRunning() {
        return false;
      }

      @Override
      public int getPhase() {
        return 1;
      }
    }
    repo.registerLifecycle(new PhasePlus1());
    repo.registerLifecycle(new Simple1());
    repo.registerLifecycle(new PhaseMinus1());
    repo.registerLifecycle(new Simple2());
    repo.start();
    assertEquals("-1", order.get(0));
    assertEquals("Simple1", order.get(1));
    assertEquals("Simple2", order.get(2));
    assertEquals("1", order.get(3));
    order.clear();
    repo.stop();
    assertEquals("1", order.get(0));
    assertEquals("Simple2", order.get(1));
    assertEquals("Simple1", order.get(2));
    assertEquals("-1", order.get(3));
  }

  /**
   *
   */
  public void testRegisterSCAware() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    final MockInterfaces mock = new MockInterfaces();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
    assertEquals(0, mock._servletContexts);
    repo.setServletContext(new MockServletContext());
    assertEquals(1, mock._servletContexts);
  }

  /**
   *
   */
  public void testRegisterInitializingBean() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    final MockInterfaces mock = new MockInterfaces();
    assertEquals(0, mock._inits);
    repo.registerComponent(info, mock);
    assertEquals(1, mock._inits);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
  }

  /**
   *
   */
  public void testRegisterFactoryBean() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    final MockFactory mock = new MockFactory();
    assertEquals(0, mock._inits);
    assertEquals(0, mock._created._inits);
    repo.registerComponent(info, mock);
    assertEquals(1, mock._inits);
    assertEquals(1, mock._created._inits);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock._created, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock._created, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
  }

  /**
   *
   */
  @Test(expectedExceptions = RuntimeException.class)
  public void testRegisterAfterStart() {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    final ComponentInfo info = new ComponentInfo(MockSimple.class, "test");
    repo.registerComponent(info, new MockSimple());
    repo.start();
    repo.registerComponent(info, new MockSimple());
  }

  /**
   * Test that we can register MBeans on the server and access their attributes.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testRegisterMBean() throws Exception {

    final MBeanServer server = createMBeanServer();

    final ComponentRepository repo = createComponentRepository(server);

    final ObjectName registrationName = new ObjectName("test:name=MBean");
    repo.registerMBean(new TestMBean(), registrationName);
    repo.start();

    assertEquals(true, server.isRegistered(registrationName));
    assertEquals(server.getAttribute(registrationName, "Answer"), 42);
  }

  /**
   * Test that we can register MX Beans on the server and access their attributes. MX Bean attributes should be converted to composite data types.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testRegisterMXBean() throws Exception {

    final MBeanServer server = createMBeanServer();

    final ComponentRepository repo = createComponentRepository(server);

    final ObjectName registrationName = new ObjectName("test:name=MXBean");
    repo.registerMBean(new TestMXBean(), registrationName);
    repo.start();

    assertEquals(true, server.isRegistered(registrationName));

    // Real test is whether we can access "remotely" - we should get type of
    // CompositeData rather than ComplexAttribute
    final CompositeData data = (CompositeData) server.getAttribute(registrationName, "Answer");

    assertEquals(42, data.get("inty"));
    assertEquals("forty-two", data.get("stringy"));
  }

  private ComponentRepository createComponentRepository(final MBeanServer server) {
    final ComponentRepository repo = new ComponentRepository(LOGGER);
    // Register the MBean server
    repo.registerComponent(MBeanServer.class, "", server);
    return repo;
  }

  private MBeanServer createMBeanServer() {
    final MBeanServerFactoryBean factoryBean = new MBeanServerFactoryBean();
    factoryBean.setLocateExistingServerIfPossible(true);

    // Ensure the server is created
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
  }

  /**
   *
   */
  public static class TestMBean {

    private final int _answer = 42;

    public int getAnswer() {
      return _answer;
    }
  }

  /**
   *
   */
  public static class TestMXBean implements TestMXInterface {

    private final ComplexAttribute _answer = new ComplexAttribute();

    @Override
    public ComplexAttribute getAnswer() {
      return _answer;
    }
  }

  /**
   *
   */
  @MXBean
  public interface TestMXInterface {
    ComplexAttribute getAnswer();
  }

  /**
   * Standard MBean can't handle this without having the defintion on the client side as well. MX Beans should be able to handle
   */
  public static class ComplexAttribute {

    public String getStringy() {
      return "forty-two";
    }

    public int getInty() {
      return 42;
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  static class MockSimple {
  }

  /**
   *
   */
  static class MockInterfaces implements Lifecycle, ServletContextAware, InitializingBean {
    private int _starts;
    private int _stops;
    private int _servletContexts;
    private int _inits;

    @Override
    public void start() {
      _starts++;
    }

    @Override
    public void stop() {
      _stops++;
    }

    @Override
    public boolean isRunning() {
      return false;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
      _servletContexts++;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
      _inits++;
    }
  }

  static class MockFactory extends SingletonFactoryBean<MockInterfaces> implements Lifecycle {
    private int _starts;
    private int _stops;
    private int _inits;
    private final MockInterfaces _created = new MockInterfaces();

    @Override
    public void start() {
      _starts++;
    }

    @Override
    public void stop() {
      _stops++;
    }

    @Override
    public boolean isRunning() {
      return false;
    }

    @Override
    public void afterPropertiesSet() {
      _inits++;
      super.afterPropertiesSet();
    }

    @Override
    protected MockInterfaces createObject() {
      return _created;
    }
  }

}
