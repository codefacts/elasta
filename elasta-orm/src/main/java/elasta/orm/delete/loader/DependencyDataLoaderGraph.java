package elasta.orm.delete.loader;

import java.util.Collection;
import java.util.Map;

/**
 * Created by sohan on 3/5/2017.
 */
public interface DependencyDataLoaderGraph {
    
    Collection<DependencyDataLoader> get(String table);

    Map<String, Collection<DependencyDataLoader>> asMap();
}
