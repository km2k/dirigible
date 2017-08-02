package org.eclipse.dirigible.runtime.databases.processor.format;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class StringRowFormatter implements RowFormatter<String> {

	private static final String VALUE_NULL = "[NULL]";
	private static final String VALUE_BINARY = "[BINARY]";
	public static final int[] BINARY_TYPES = new int[] { java.sql.Types.ARRAY, java.sql.Types.BINARY, java.sql.Types.BIT, java.sql.Types.BIT,
			java.sql.Types.BLOB, java.sql.Types.CLOB, java.sql.Types.DATALINK, java.sql.Types.DISTINCT, java.sql.Types.JAVA_OBJECT,
			java.sql.Types.LONGVARBINARY, java.sql.Types.NCLOB, java.sql.Types.NULL, java.sql.Types.OTHER, java.sql.Types.REF, java.sql.Types.SQLXML,
			java.sql.Types.STRUCT, java.sql.Types.VARBINARY };

	@Override
	public String write(List<ColumnDescriptor> columnDescriptors, ResultSetMetaData resultSetMetaData, ResultSet resultSet) throws SQLException {
		StringBuilder buff = new StringBuilder();
		buff.append(ResultSetStringWriter.DELIMITER);
		for (ColumnDescriptor columnDescriptor : columnDescriptors) {

			String value = null;

			// For the schemaless NoSQL DBs it's perfectly legal for records to miss some key-value tuples.
			if (columnDescriptor.getSqlType() == Integer.MIN_VALUE) {
				value = "";
			} else {
				if (this.isBinaryType(columnDescriptor.getSqlType())) {
					value = VALUE_BINARY;
				} else {
					value = resultSet.getString(columnDescriptor.getLabel());
				}

				if (value == null) {
					value = VALUE_NULL;
				}

				if (!VALUE_BINARY.equals(value) || !VALUE_NULL.equals(value)) {

					int delta = value.length() - columnDescriptor.getDisplaySize();
					if (delta > 0) {
						value = value.substring(0, columnDescriptor.getDisplaySize());
						if (value.length() > 3) {
							value = value.substring(0, value.length() - 3) + "...";
						}
					} else if (delta < 0) {
						value = String.format("%-" + columnDescriptor.getDisplaySize() + "s", value);
					}

				}
			}

			buff.append(value);
			buff.append(ResultSetStringWriter.DELIMITER);

		}
		buff.append(ResultSetStringWriter.NEWLINE_CHARACTER);
		return buff.toString();
	}

	boolean isBinaryType(int columnType) {
		for (int c : BINARY_TYPES) {
			if (columnType == c) {
				return true;
			}
		}
		return false;
	}

}