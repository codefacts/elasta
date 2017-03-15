package elasta.orm.delete.impl;

import elasta.orm.delete.DeleteContext;import elasta.orm.delete.DeleteContext;
import elasta.orm.upsert.TableData;

/**
 * Created by Jango on 17/02/16.
 */
public interface PIndirectDeleteHandler {
    void delete(TableData childEntity, DeleteContext context);
}
