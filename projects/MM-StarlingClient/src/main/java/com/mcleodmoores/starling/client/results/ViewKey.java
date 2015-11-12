package com.mcleodmoores.starling.client.results;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A key for identifying views.  When you query the system, it may include ids to speed up resolution and handle
 * duplicate names.  When searching, just create a key using the single arg of().
 */
public class ViewKey {

  private final UniqueId _uniqueId;
  private final String _name;

  private ViewKey(String name, UniqueId uniqueId) {
    _uniqueId = uniqueId;
    _name = ArgumentChecker.notNull(name, "name");
  }

  /**
   * Static factory method used to create key instances when the uniqueId of the view is not known.
   * @param name  the name of the view, not null
   * @return the position target key, not null
   */
  public static ViewKey of(String name) {
    return new ViewKey(name, null);
  }

  /**
   * Static factory method used to create key instances, typically when the uniqueId is known.
   * @param name  the name of the view, not null
   * @param uniqueId  the unique id of the view, if known, null otherwise
   * @return the position target key, not null
   */
  public static ViewKey of(String name, UniqueId uniqueId) {
    return new ViewKey(name, uniqueId);
  }

  /**
   * @return the unique id of the view, if known, null otherwise.
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * @return true, if the key contains a unique id (which can speed up lookup and handle duplicates better).
   */
  public boolean hasUniqueId() {
    return _uniqueId != null;
  }

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
    if (!(o instanceof ViewKey)) {
      return false;
    }
    ViewKey other = (ViewKey) o;
    return other.getName().equals(getName());
  }

  @Override
  public String toString() {
    return "ViewKey[" + _name + (hasUniqueId() ? ("(" + _uniqueId.toString() + ")]") : "]");
  }
}
