package de.braintags.io.vertx.pojomapper.dataaccess.query.impl;

import de.braintags.io.vertx.pojomapper.dataaccess.query.ISearchCondition;
import de.braintags.io.vertx.pojomapper.dataaccess.query.QueryLogic;

/**
 * Represents a container that joins search conditions with an {@link QueryLogic#AND}<br>
 * <br>
 * Copyright: Copyright (c) 20.12.2016 <br>
 * Company: Braintags GmbH <br>
 * 
 * @author sschmitt
 */
public class QueryAnd extends AbstractSearchConditionContainer {

  /**
   * Initializes the container with zero or more sub conditions that will be connected with {@link QueryLogic#AND}
   * 
   * @param conditions
   */
  public QueryAnd(ISearchCondition... searchConditions) {
    super(searchConditions);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.ISearchConditionContainer#getQueryLogic()
   */
  @Override
  public QueryLogic getQueryLogic() {
    return QueryLogic.AND;
  }

}