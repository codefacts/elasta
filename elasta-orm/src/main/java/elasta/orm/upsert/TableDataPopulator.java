package elasta.orm.upsert;

import io.vertx.core.json.JsonObject;

/**
 * Created by Jango on 2017-01-09.
 */
public interface TableDataPopulator {
    TableData populate(JsonObject jsonObject);
}
