package elasta.orm.entity.core.columnmapping.impl;

import elasta.orm.entity.core.RelationType;
import elasta.orm.entity.core.ForeignColumnMapping;
import elasta.orm.entity.core.columnmapping.DirectRelationMapping;
import elasta.orm.entity.core.columnmapping.DirectRelationMappingOptions;
import lombok.Value;

import java.util.List;
import java.util.Objects;

/**
 * Created by Jango on 2017-01-12.
 */
@Value
final public class DirectRelationMappingImpl implements DirectRelationMapping {
    final String referencingTable;
    final String referencingEntity;
    final List<ForeignColumnMapping> foreignColumnMappingList;
    final String field;
    final RelationType columnType;
    final DirectRelationMappingOptions options;

    public DirectRelationMappingImpl(String referencingTable, String referencingEntity, List<ForeignColumnMapping> foreignColumnMappingList, String field, DirectRelationMappingOptions options) {
        Objects.requireNonNull(referencingTable);
        Objects.requireNonNull(referencingEntity);
        Objects.requireNonNull(foreignColumnMappingList);
        Objects.requireNonNull(field);
        Objects.requireNonNull(options);
        this.referencingTable = referencingTable;
        this.referencingEntity = referencingEntity;
        this.foreignColumnMappingList = foreignColumnMappingList;
        this.field = field;
        this.columnType = RelationType.DIRECT;
        this.options = options;
    }

    @Override
    public String getReferencingTable() {
        return referencingTable;
    }

    public String getReferencingEntity() {
        return referencingEntity;
    }

    @Override
    public DirectRelationMappingOptions getOptions() {
        return options;
    }

    @Override
    public List<ForeignColumnMapping> getForeignColumnMappingList() {
        return foreignColumnMappingList;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public RelationType getColumnType() {
        return columnType;
    }

}
