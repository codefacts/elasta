package elasta.orm.delete.impl;

import elasta.orm.delete.ListTablesToDeleteContext;
import elasta.orm.upsert.TableData;

import java.util.Objects;
import java.util.Set;

/**
 * Created by sohan on 3/12/2017.
 */
final public class ListTablesToDeleteContextImpl implements ListTablesToDeleteContext {
    final Set<TableData> tableDataSet;

    public ListTablesToDeleteContextImpl(Set<TableData> tableDataSet) {
        Objects.requireNonNull(tableDataSet);
        this.tableDataSet = tableDataSet;
    }

    @Override
    public void add(TableData tableData) {
        tableDataSet.add(tableData);
    }
}
