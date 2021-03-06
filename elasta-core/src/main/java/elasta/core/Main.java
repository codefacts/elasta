package elasta.core;

import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Defer;
import elasta.core.promise.intfs.Promise;

/**
 * Created by Jango on 8/31/2016.
 */
public class Main {
    public static void main(String[] args) {

        Promises.of("ok")
            .mapP(val -> {
                return Promises.of("mapP >> " + val)
                    .then(val1 -> {
                        System.out.println("mapP: " + val1);
                    });
            })
            .then(val -> {
                System.out.println("end: " + val);
            })
            .cmp(signal -> {
                System.out.println(signal);
            })
            .err(val -> {
                val.printStackTrace();
            })
        ;

    }

    private static void test7() {
        Promises.of("ok")
            .then(val -> System.out.println("val: " + val))
            .mapP(s -> Promises.of(s + " then"))
            .then(val -> System.out.println("mapP: " + val))
            .cmpP(
                signal ->
                    Promises
                        .of(signal.val() + " complete")
                        .then(val -> System.out.println("cmpP: " + val))
                        .map(Promises.toVoid())
            )
            .then(val -> System.out.println("end"))
        ;
    }

    private static void test6() {

        Defer<Object> defer = Promises.defer();

        {
            defer.reject(new NullPointerException());
            defer.resolve("ok");
        }

        Promise<Object> promise1 = defer.promise();
        promise1.then(val -> {
        });

        Promises.of(978)
            .filter(integer -> integer.equals(868))
            .then(val -> System.out.println("Value Got: " + val))
            .err(Throwable::printStackTrace)
            .cmp(promise -> System.out.println("COmplete"))
        ;
    }

    public void test5() {
        Promises.of("val")
            .filter(o -> true)
            .map(v -> v.toUpperCase())
            .filter(o -> o == "ok")
            .err(val -> {
                System.out.println("error: " + val);

            })
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .map(val -> "[" + val + "]")
            .then(p -> System.out.println("p: " + p))
            .filter(o -> false)
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .filter(o -> true)
            .cmp(v -> System.out.println("ok: " + v))
        ;
    }

    public void test4() {
        Promises.of("val")
            .filter(o -> true)
            .filter(o -> true)
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .filter(o -> false)
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .filter(o -> true)
            .cmp(v -> System.out.println("ok: " + v))
        ;
    }

    public void test3() {
        Promises.error(new NullPointerException("catch it"))
            .filter(o -> false)
            .filter(o -> false)
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .filter(o -> false)
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .filter(o -> true)
            .cmp(v -> System.out.println("ok: " + v))
        ;
    }

    public void test2() {
        Promises.error(new NullPointerException("catch it"))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
        ;
    }

    public void test1() {
        Promises.empty()
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
        ;

        Promises.of("value")
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
            .err(val -> System.out.println("error: " + val))
            .then(p -> System.out.println("p: " + p))
            .cmp(v -> System.out.println("ok: " + v))
        ;
    }
}
