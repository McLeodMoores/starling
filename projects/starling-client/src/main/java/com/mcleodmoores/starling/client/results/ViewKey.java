package com.mcleodmoores.starling.client.results;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A key for identifying views.  When the system is queried, it may include ids to speed up resolution and handle
 * duplicate names.  When searching, only the name is required and so {@link ViewKey#of(String)} should be used.
 */
public class ViewKey {
  /** The unique id of the view */
  private final UniqueId _uniqueId;
  /** The name of the view */
  private final String _name;

  /**
   * Restricted constructor.
   * @param name  the name of the view, not null
   * @param uniqueId  the unique id of the view, can be null
   */
  private ViewKey(final String name, final UniqueId uniqueId) {
    _uniqueId = uniqueId;
    _name = ArgumentChecker.notNull(name, "name");
  }

  /**
   * Static factory method used to create key instances when the uniqueId of the view is not known.
   * @param name  the name of the view, not null
   * @return the view key, not null
   */
  public static ViewKey of(final String name) {
    return new ViewKey(name, null);
  }

  /**
   * Static factory method used to create key instances, typically when the uniqueId is known.
   * @param name  the name of the view, not null
   * @param uniqueId  the unique id of the view, if known, null otherwise
   * @return the view key, not null
   */
  public static ViewKey of(final String name, final UniqueId uniqueId) {
    return new ViewKey(name, uniqueId);
  }

  /**
   * Static factory method used to create a key instance from information stored in a view definition.
   * @param viewDefinition  the view definition, not null
   * @return  the view key
   */
  public static ViewKey of(final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    return new ViewKey(viewDefinition.getName(), viewDefinition.getUniqueId());
  }

  /**
   * Gets the unique id of the view.
   * @return the unique id of the view, if known, null otherwise
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Returns true if the key contains a unique id, which can speed up lookup and handle duplicates better.
   * @return true, if the key contains a unique id
   */
  public boolean hasUniqueId() {
    return _uniqueId != null;
  }

  /**
   * Gets the name of the view definition.
   * @return  the name of the view definition.
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewKey)) {
      return false;
    }
    final ViewKey other = (ViewKey) o;
    return other.getName().equals(getName());
  }

  @Override
  public String toString() {
    return "ViewKey[" + _name + (hasUniqueId() ? "(" + _uniqueId.toString() + ")]" : "]");
  }
}
