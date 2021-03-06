package elasta.pipeline.transformation.impl.json.object;

import elasta.pipeline.transformation.JoJoTransformation;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Created by shahadat on 3/5/16.
 */
public class RemoveNullsTransformation implements JoJoTransformation {
    private final RecursiveMerge recursiveMerge;

    public RemoveNullsTransformation() {
        this(null, null);
    }

    public RemoveNullsTransformation(Set<List<String>> includes, Set<List<String>> excludes) {
        recursiveMerge = new RecursiveMerge(includes, excludes,
            o -> o == null, (e, remove) -> remove.run(),
            o -> o == null, (v, remove) -> remove.run());
    }

    @Override
    public JsonObject transform(JsonObject json) {

        if (json == null) return null;

        JsonObject transform = recursiveMerge.transform(json);
        return transform;
    }
}
