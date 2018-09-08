package org.openaudible.desktop.swt.gui.tables;


public abstract class MultiColumnData extends SingleColumnData {
	public MultiColumnData(Comparable<?> c) {
		super(c);
	}
	
	@Override
	public abstract String getTextValue(SuperTable table, int col);
	
	/*
	 * { switch (col) { case 0: return data.toString(); default: return ""; }
	 *
	 * }
	 */
	
	public abstract int getSortCol();
	
	@Override
	public int compareTo(SuperTable table, SuperTableData that, int col) {
		String x = getTextValue(table, col);
		String y = that.getTextValue(table, col);
		return x.compareTo(y);
	}
	
	@Override
	public int compareTo(SuperTableData t) {
		if (data == null || t == null) {
			return -1;
		}
		assert (false);
		// log.error("compareTo called for MultiColumnData. Should call other method.");
		return -1;
		
		// if (reverseSort)
		// return ((SingleColumnData)t).getData().compareTo(data);
		// return compareTo(t, getSortCol());
	}
	
}
