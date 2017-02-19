package elasta.orm.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.core.intfs.Consumer1Unckd;
import elasta.core.intfs.Fun1Unckd;
import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Defer;
import elasta.core.promise.intfs.Promise;
import elasta.core.touple.immutable.Tpl2;
import elasta.core.touple.immutable.Tpls;
import elasta.vertxutils.VertxUtils;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jango on 10/2/2016.
 */
public class JpaImpl implements Jpa {
    private final Vertx vertx;
    private final EntityManagerFactory emf;
    private final ObjectMapper mapper;
    private final Map<String, Class> entityClassMap;

    public JpaImpl(Vertx vertx, EntityManagerFactory emf, ObjectMapper mapper, Map<String, Class> entityClassMap) {
        this.vertx = vertx;
        this.emf = emf;
        this.mapper = mapper;
        this.entityClassMap = entityClassMap;
    }

    @Override
    public <T> Class<T> getModelClass(String model) {
        return entityClassMap.get(model);
    }

    @Override
    public <T> Promise<JsonObject> find(Class<T> tClass, Object id) {
        return exeQuery(entityManager -> {
            T obj = entityManager.find(tClass, id);
            Map map = mapper.convertValue(obj, Map.class);
            return new JsonObject(map);
        });
    }

    @Override
    public Promise<List<JsonObject>> jpqlQuery(String jpql) {
        return jpqlQuery(jpql, new JsonArray(ImmutableList.of()));
    }

    @Override
    public Promise<List<JsonObject>> jpqlQuery(String jpql, JsonArray params) {
        return exeQuery(entityManager -> {
            Query query = entityManager.createQuery(jpql);

            for (int i = 0; i < params.size(); i++) {
                query.setParameter(i + 1, params.getValue(i));
            }

            List<Map<String, Object>> maps = mapper.convertValue(query.getResultList(), listOfMap());
            return maps.stream().map(JsonObject::new).collect(Collectors.toList());
        });
    }

    @Override
    public Promise<JsonObject> jpqlQuerySingle(String jpql) {
        return jpqlQuerySingle(jpql, new JsonArray(Collections.emptyList()));
    }

    @Override
    public Promise<JsonObject> jpqlQuerySingle(String jpql, JsonArray params) {
        return jpqlQuery(jpql, params).map(jsonObjects -> jsonObjects.get(0));
    }

    @Override
    public Promise<List<JsonArray>> jpqlQueryArray(String jpql) {
        return jpqlQueryArray(jpql, new JsonArray(ImmutableList.of()));
    }

    @Override
    public Promise<List<JsonArray>> jpqlQueryArray(String jpql, JsonArray params) {
        return exeQuery(entityManager -> {
            TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
            for (int i = 0; i < params.getList().size(); i++) {
                query.setParameter(i + 1, params.getValue(i));
            }
            List list = query.getResultList();
            if (list.size() <= 0) {
                return ImmutableList.of();
            }
            if (list.get(0).getClass() == Object[].class) {
                List<List<Object>> lists = mapper.convertValue(list, listOfList());
                return lists.stream().map(JsonArray::new).collect(Collectors.toList());
            }
            ImmutableList.Builder<JsonArray> listBuilder = ImmutableList.builder();

            list.forEach(object -> listBuilder.add(new JsonArray(ImmutableList.of(object))));

            return listBuilder.build();
        });
    }

    @Override
    public Promise<JsonArray> jpqlQuerySingleArray(String jpql) {
        return jpqlQueryArray(jpql).map(jsonArrays -> jsonArrays.get(0));
    }

    @Override
    public Promise<JsonArray> jpqlQuerySingleArray(String jpql, JsonArray params) {
        return jpqlQueryArray(jpql, params).map(jsonArrays -> jsonArrays.get(0));
    }

    @Override
    public <T> Promise<T> jpqlQueryScalar(String jpql, Class<T> tClass) {
        return exeQuery(entityManager -> entityManager.createQuery(jpql, tClass).getSingleResult());
    }

    @Override
    public <T> Promise<T> jpqlQueryScalar(String jpql, Class<T> tClass, JsonArray params) {
        return exeQuery(entityManager -> {
            TypedQuery<T> query = entityManager.createQuery(jpql, tClass);
            List list = params.getList();
            for (int i = 0, listSize = list.size(); i < listSize; i++) {
                query.setParameter(i + 1, list.get(i));
            }
            return query.getSingleResult();
        });
    }

    @Override
    public <T> Promise<List<JsonObject>> query(Fun1Unckd<CriteriaBuilder, CriteriaQuery<T>> fun1Unckd) {
        return exeQuery(entityManager -> {

            List<T> list = entityManager.createQuery(fun1Unckd.apply(entityManager.getCriteriaBuilder())).getResultList();

            List<Map<String, Object>> maps = mapper.convertValue(list, listOfMap());

            return maps.stream().map(JsonObject::new).collect(Collectors.toList());
        });
    }

    @Override
    public Promise<List<JsonArray>> queryArray(Fun1Unckd<CriteriaBuilder, CriteriaQuery<Object[]>> fun1Unckd) {
        return exeQuery(em -> {
            CriteriaQuery criteriaQuery = fun1Unckd.apply(em.getCriteriaBuilder());

            List<Object> list = em.createQuery(criteriaQuery).getResultList();

            if (list.size() <= 0) {
                return ImmutableList.of();
            }

            if (list.get(0).getClass() == Object[].class) {

                List<List> lists = mapper.convertValue(list, listOfList());

                return lists.stream().map(JsonArray::new).collect(Collectors.toList());
            }
            ImmutableList.Builder<JsonArray> listBuilder = ImmutableList.builder();
            list.forEach(element -> listBuilder.add(new JsonArray(ImmutableList.of(element))));
            return listBuilder.build();
        });
    }

    @Override
    public <T> Promise<JsonObject> querySingle(Fun1Unckd<CriteriaBuilder, CriteriaQuery<T>> fun1Unckd) {
        return exeQuery(entityManager -> {
            CriteriaQuery<T> query = fun1Unckd.apply(entityManager.getCriteriaBuilder());
            T result = entityManager.createQuery(query).getSingleResult();
            Map map = mapper.convertValue(result, Map.class);
            return new JsonObject(map);
        });
    }

    @Override
    public Promise<JsonArray> querySingleArray(Fun1Unckd<CriteriaBuilder, CriteriaQuery<Object[]>> fun1Unckd) {
        return exeQuery(entityManager -> {
            CriteriaQuery<Object[]> query = fun1Unckd.apply(entityManager.getCriteriaBuilder());
            Object[] result = entityManager.createQuery(query).getSingleResult();
            List list = mapper.convertValue(result, List.class);
            return new JsonArray(list);
        });
    }

    @Override
    public <T> Promise<T> queryScalar(Fun1Unckd<CriteriaBuilder, CriteriaQuery<T>> fun1Unckd) {
        return exeQuery(entityManager -> {

            CriteriaQuery<T> query = fun1Unckd.apply(entityManager.getCriteriaBuilder());

            return entityManager.createQuery(query).getSingleResult();
        });
    }

    @Override
    public Promise<Void> update(Fun1Unckd<CriteriaBuilder, CriteriaQuery> fun1Unckd) {
        return exeUpdate(entityManager -> {
            CriteriaQuery query = fun1Unckd.apply(entityManager.getCriteriaBuilder());
            entityManager.createQuery(query).executeUpdate();
        });
    }

    @Override
    public Promise<Void> update(List<Fun1Unckd<CriteriaBuilder, CriteriaQuery>> fun1UnckdList) {
        return exeUpdate(entityManager -> {

            for (Fun1Unckd<CriteriaBuilder, CriteriaQuery> fun1Unckd : fun1UnckdList) {
                CriteriaQuery query = fun1Unckd.apply(entityManager.getCriteriaBuilder());

                entityManager.createQuery(query).executeUpdate();
            }

        });
    }

    private <T> Promise<T> exeQuery(Fun1Unckd<EntityManager, T> fun1Unckd) {
        Defer<T> defer = Promises.defer();

        vertx.executeBlocking(queryHandler(fun1Unckd), VertxUtils.deferred(defer));
        return defer.promise();
    }

    private Promise<Void> exeUpdate(Consumer1Unckd<EntityManager> consumer1Unckd) {
        Defer<Void> defer = Promises.defer();
        vertx.executeBlocking(updateHandler(consumer1Unckd), VertxUtils.deferred(defer));
        return defer.promise();
    }

    private <T> Handler<Future<T>> queryHandler(Fun1Unckd<EntityManager, T> fun1Unckd) {
        return future -> {

            EntityManager em = null;

            try {

                em = emf.createEntityManager();

                T result = fun1Unckd.apply(em);

                future.complete(result);

            } catch (Throwable e) {

                future.fail(e);

            } finally {

                if (em != null) {
                    em.close();
                }
            }

        };
    }

    private <T> Handler<Future<T>> updateHandler(Consumer1Unckd<EntityManager> consumer1Unckd) {
        return future -> {

            EntityManager em = null;

            try {

                em = emf.createEntityManager();

                transaction(em, consumer1Unckd, future);

            } catch (Throwable e) {

                future.fail(e);

            } finally {

                if (em != null) {
                    em.close();
                }
            }

        };
    }

    private void transaction(EntityManager em, Consumer1Unckd<EntityManager> consumer1Unckd, Future future) throws Throwable {

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            consumer1Unckd.accept(em);

            tx.commit();

            future.complete();

        } catch (PersistenceException ex) {

            future.fail(ex);

            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    private TypeReference<List<Map<String, Object>>> listOfMap() {
        return new ListOfMap();
    }

    private TypeReference<List<List<Object>>> listOfList() {
        return new ListOfList();
    }

    private static class ListOfMap extends TypeReference<List<Map<String, Object>>> {
    }

    private static class ListOfList extends TypeReference<List<List<Object>>> {
    }

    public static void main(String[] args) throws IOException {
        test2();
    }

    private static void test2() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(DateFormat.getDateTimeInstance());

        Object[] objects = {
            "sohan",
            Tpls.of("1", "2"),
            12,
            Tpls.of("1", "2", "3"),
            true,
            new Date(),
            Tpls.of("1", "2", "3", "4"),
            new String[]{"kona", "money"},
            new Integer[]{1, 2, 4},
            ImmutableList.builder().add(1).add(Tpls.of(1, 3)).build(),
            ImmutableMap.builder().put("k", "kv").put("date", new Date()).build()
        };

        List<List> lists = mapper.convertValue(ImmutableList.of(objects, objects), new ListOfList());

        lists.forEach(list -> {
            list.forEach(o -> {
                System.out.println(o.getClass() + ": " + o);
            });
        });
    }

    private static void test1() {
        Tpl2<String, String> of = Tpls.of("1", "2");
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("started");
        final int ite = 100_000;
        long t1 = System.nanoTime();
        for (int i = 0; i < ite; i++) {
            List<Map<String, Object>> value = mapper.convertValue(ImmutableList.of(of), new TypeReference<List<Map<String, Object>>>() {
            });
        }

        long t2 = System.nanoTime();
        long time = t2 - t1;
        System.out.println("total time: " + time / 1000_000);
        System.out.println("time: " + (time / ite) / 1000);
    }

}
