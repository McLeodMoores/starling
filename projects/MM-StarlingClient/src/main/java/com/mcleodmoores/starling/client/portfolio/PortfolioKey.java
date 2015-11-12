package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A key for identifying portfolios.  When you query the system, it may include ids to speed up resolution and handle
 * duplicate names.  When searching, just create a key using the single arg of().
 */
public class PortfolioKey {

  private final UniqueId _uniqueId;
  private final String _name;

  private PortfolioKey(String name, UniqueId uniqueId) {
    _name = ArgumentChecker.notNull(name, "name");
    _uniqueId = uniqueId;
  }

  /**
   * Static factory method used to create instances of PortfolioKey.  This method should be used when you don't know the
   * unique id of the portfolio.
   * @param name  the name of the portfolio, not null
   * @return the portfolio key, not null
   */
  public static PortfolioKey of(String name) {
    return new PortfolioKey(name, null);
  }

  /**
   * Static factory method used to create instances of PortfolioKey.  This method should be used when you do know the
   * unique id of the portfolio, although null can be passed to the uniqueId argument.
   * @param name  the name of the portfolio, not null
   * @param uniqueId  the unique id of the portfolio, or null if not known
   * @return the portfolio key, not null
   */
  public static PortfolioKey of(String name, UniqueId uniqueId) {
    return new PortfolioKey(name, uniqueId);
  }

  /**
   * @return the unique id of the portfolio, or null if not known when this key was created
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * @return true, if this key contains a unique id for the portfolio
   */
  public boolean hasUniqueId() {
    return _uniqueId != null;
  }

  /**
   * @return the name of the portfolio, not null
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof PortfolioKey)) {
      return false;
    }
    PortfolioKey other = (PortfolioKey) o;
    return other.getName().equals(getName());
  }

  @Override
  public String toString() {
    return "PortfolioKey[" + _name + (hasUniqueId() ? ("(" + _uniqueId.toString() + ")]") : "]");
  }
}
