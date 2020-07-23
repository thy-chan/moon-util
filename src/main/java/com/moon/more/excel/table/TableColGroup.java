package com.moon.more.excel.table;

import com.moon.core.lang.ref.IntAccessor;
import com.moon.more.excel.RowFactory;
import com.moon.more.excel.annotation.style.StyleBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author moonsky
 */
final class TableColGroup extends TableCol {

    private final Class targetClass;
    private final TableRenderer child;

    TableColGroup(AttrConfig config, TableRenderer child) {
        super(config);
        this.child = child;
        this.targetClass = config.getTargetClass();
    }

    @Override
    int getDepth() { return child.getDepth() + 1; }

    @Override
    int getCrossColsCount() { return child.getHeaderColsCount(); }

    @Override
    int getHeaderRowsCount() { return super.getHeaderRowsCount() + child.getHeaderRowsCount(); }

    @Override
    void collectStyleMap(
        Map<Class, Map<String, StyleBuilder>> definitions, Map sourceMap
    ) { child.collectStyleMap(definitions, sourceMap); }

    @Override
    void appendColumnWidth(List<Integer> columnsWidth) {
        super.appendOffsetWidth(columnsWidth);
        child.appendColumnWidth(columnsWidth);
    }

    @Override
    void appendTitlesAtRowIdx(List<HeadCell> rowTitles, int rowIdx) {
        appendTitles4Offset(rowTitles, rowIdx);
        int superRowsLength = super.getHeaderRowsLength();
        if (rowIdx < superRowsLength) {
            int colsCount = child.getHeaderColsCount();
            HeadCell headCell = headCellAtIdx(rowIdx);
            for (int i = 0; i < colsCount; i++) {
                rowTitles.add(headCell);
            }
        } else {
            child.appendTitlesAtRowIdx(rowTitles, rowIdx - superRowsLength);
        }
    }

    @Override
    void render(TableProxy proxy) {
        proxy.startLocalDataNode(getControl(), targetClass);
        if (proxy.isSkipped()) {
            proxy.skip(getOffset(), isFillSkipped());
        } else if (getOffset() > 0) {
            proxy.doOffsetCells(getOffset(), isFillSkipped());
            // proxy.indexedCell(getOffset(), isFillSkipped());
            // System.out.println();
        }
        child.doRenderRow(proxy);
        proxy.closeLocalDataNode();
    }

    @Override
    void render(IntAccessor indexer, RowFactory factory, Object data) {
        Object entityData = getControl().control(data);
        child.doRenderRow(indexer, factory, entityData);
    }
}
