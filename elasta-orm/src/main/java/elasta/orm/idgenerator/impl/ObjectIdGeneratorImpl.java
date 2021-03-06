package elasta.orm.idgenerator.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Promise;
import elasta.orm.entity.EntityMappingHelper;
import elasta.orm.idgenerator.IdGenerator;
import elasta.orm.idgenerator.ObjectIdGenerator;
import elasta.orm.idgenerator.ex.ObjectIdGeneratorException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sohan on 6/28/2017.
 */
final public class ObjectIdGeneratorImpl<T> implements ObjectIdGenerator<T> {
    final EntityMappingHelper helper;
    final IdGenerator<T> idGenerator;
    final String isNewKey;

    public ObjectIdGeneratorImpl(EntityMappingHelper helper, IdGenerator<T> idGenerator, String isNewKey) {
        Objects.requireNonNull(helper);
        Objects.requireNonNull(idGenerator);
        Objects.requireNonNull(isNewKey);
        this.helper = helper;
        this.idGenerator = idGenerator;
        this.isNewKey = isNewKey;
    }

    @Override
    public Promise<JsonObject> generateId(String entity, JsonObject jsonObject) {

        List<Promise<AbstractMap.SimpleImmutableEntry<String, Object>>> promises = helper.getRelationMappings(entity).stream()
            .filter(relationMapping -> jsonObject.getValue(relationMapping.getField()) != null)
            .map(relationMapping -> {

                String field = relationMapping.getField();
                Object value = jsonObject.getValue(relationMapping.getField());

                return processValue(relationMapping.getReferencingEntity(), value)
                    .map(newValue -> new AbstractMap.SimpleImmutableEntry<>(field, newValue));

            }).collect(Collectors.toList());

        return Promises.when(promises)
            .mapP(simpleImmutableEntries -> toImmutableMap(simpleImmutableEntries, entity, jsonObject))
            .map(immutableMap -> {

                if (immutableMap.isEmpty()) {
                    return jsonObject;
                }

                return new JsonObject(
                    ImmutableMap.<String, Object>builder()
                        .putAll(minus(jsonObject.getMap(), immutableMap))
                        .putAll(immutableMap).build()
                );
            });
    }

    private Map<String, Object> minus(Map<String, Object> map1, Map<String, Object> map2) {

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        map1.entrySet().stream()
            .filter(entry -> !map2.containsKey(entry.getKey()))
            .forEach(entry -> {
                builder.put(entry.getKey(), entry.getValue());
            });

        return builder.build();
    }

    private Promise<Object> processValue(String referencingEntity, Object value) {

        if (value instanceof JsonObject) {

            return traverseObject(referencingEntity, ((JsonObject) value));

        } else if (value instanceof Map) {

            return traverseObject(referencingEntity, new JsonObject(castToMap(value)));

        } else if (value instanceof JsonArray) {

            return traverseArray(referencingEntity, ((JsonArray) value));


        } else if (value instanceof List) {

            return traverseArray(referencingEntity, new JsonArray(castToList(value)));

        } else {

            return Promises.of(value);
        }
    }

    private Promise<Map<String, Object>> toImmutableMap(List<AbstractMap.SimpleImmutableEntry<String, Object>> simpleImmutableEntries, String entity, JsonObject jsonObject) {

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        final String primaryKey = helper.getPrimaryKey(entity);

        if (jsonObject.getValue(primaryKey) == null) {

            if (jsonObject.containsKey(isNewKey)) {
                throw new ObjectIdGeneratorException("IsNewKey '" + isNewKey + "' already exists");
            }

            builder.put(isNewKey, true);
            return idGenerator.nextId(entity)
                .map(newId -> {
                    builder.put(primaryKey, newId);
                    return builder.putAll(simpleImmutableEntries).build();
                });
        }

        return Promises.of(
            builder.putAll(simpleImmutableEntries).build()
        );
    }

    private Promise<Object> traverseObject(String entity, JsonObject jsonObject) {
        return generateId(entity, jsonObject).map(jso -> jso);
    }

    private Promise<Object> traverseArray(String entity, JsonArray jsonArray) {

        List<Promise<Object>> promises = jsonArray.stream().map(object -> processValue(entity, object)).collect(Collectors.toList());

        return Promises.when(promises)
            .map(JsonArray::new);
    }

    private List castToList(Object value) {
        return ((List) value);
    }

    private Map<String, Object> castToMap(Object value) {
        return ((Map<String, Object>) value);
    }
}
