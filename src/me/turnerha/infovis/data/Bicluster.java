package me.turnerha.infovis.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Never store a reference to a Bicluster for a long time. 
// Instead, store that bicluster's ID field and constantly call 
// Cache.getBicluster. This allows the cache to delete and 
// re-create biclusters as needed to handle memory
// TODO implement some sort of flyweight or proxy pattern
public class Bicluster {
	private int bicluster_id;
	private Dimension[] mDimension; // delete-able
	private String externalDbName; // delete-able
	private ArrayList<Integer> chainedBiclusters; // delete-able
	private ArrayList<Integer> overlappedBiclusters; // delete-able

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{id=");
		sb.append(bicluster_id).append(',').append("dimension={");
		if (mDimension == null)
			sb.append("null}");
		else
			sb.append(mDimension[0]).append(',').append(mDimension[1]).append(
					'}');
		sb.append(",externalDbName={").append(externalDbName).append(
				"},chainedBiclusters={").append(chainedBiclusters).append("}}");
		return sb.toString();
	}

	protected Bicluster(int bicluster_id) {
		this.bicluster_id = bicluster_id;
	}

	public int getBiclusterId() {
		return bicluster_id;
	}

	public List<Integer> getChainedBiclusters() {
		if (chainedBiclusters != null)
			return chainedBiclusters;

		// Grab all chaining_link_ids that target us
		ResultSet rs = DBUtils
				.executeQuery("SELECT id FROM symfony.chaining_link WHERE target_bicluster_id="
						+ bicluster_id);
		StringBuffer in = new StringBuffer();
		try {
			while (rs.next()) {
				in.append(rs.getInt(1)).append(',');
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		in.setLength(in.length() - 1);

		// Grab the destinations of all of them
		rs = DBUtils
				.executeQuery("SELECT destination_bicluster_id FROM symfony.chaining_link_destination WHERE chaining_link_id IN ("
						+ in.toString() + ")");
		chainedBiclusters = new ArrayList<Integer>();
		try {
			while (rs.next()) {
				Bicluster other = Cache.getBicluster(rs.getInt(1));
				if (false == isOverlappedBicluster(other))
					chainedBiclusters.add(other.getBiclusterId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return chainedBiclusters;
	}

	public List<Integer> getOverlappedBiclusters() {
		if (overlappedBiclusters != null)
			return overlappedBiclusters;

		// Grab all chaining_link_ids that target us
		ResultSet rs = DBUtils
				.executeQuery("SELECT id FROM symfony.chaining_link WHERE target_bicluster_id="
						+ bicluster_id);
		StringBuffer in = new StringBuffer();
		try {
			while (rs.next()) {
				in.append(rs.getInt(1)).append(',');
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		in.setLength(in.length() - 1);

		// Grab the destinations of all of them
		rs = DBUtils
				.executeQuery("SELECT destination_bicluster_id FROM symfony.chaining_link_destination WHERE chaining_link_id IN ("
						+ in.toString() + ")");
		overlappedBiclusters = new ArrayList<Integer>();
		try {
			while (rs.next()) {
				Bicluster other = Cache.getBicluster(rs.getInt(1));
				if (isOverlappedBicluster(other))
					overlappedBiclusters.add(other.getBiclusterId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return overlappedBiclusters;
	}

	public boolean isOverlappedBicluster(Bicluster other) {
		Dimension[] mDim = getDimensions();
		Dimension[] oDim = other.getDimensions();

		// date x location & date x location
		if (mDim[0].getName().equals(oDim[0].getName()))
			if (mDim[1].getName().equals(oDim[1].getName()))
				return true;

		// date x location & location x date
		if (mDim[0].getName().equals(oDim[1].getName()))
			if (mDim[1].getName().equals(oDim[0].getName()))
				return true;
		return false;
	}

	public static List<Bicluster> getAllBiclusters() {
		return DBUtils.listBiclusters();
	}

	public String findCommonDimension(Bicluster other) {
		Dimension[] mDim = getDimensions();
		Dimension[] oDim = other.getDimensions();

		if (mDim[0].getName().equals(oDim[0].getName()))
			return mDim[0].getName();
		else if (mDim[0].getName().equals(oDim[1].getName()))
			return mDim[0].getName();
		else if (mDim[1].getName().equals(oDim[0].getName()))
			return mDim[1].getName();
		else if (mDim[1].getName().equals(oDim[1].getName()))
			return mDim[1].getName();

		throw new IllegalArgumentException("Biclusters are unrelated: "
				+ this.toString() + "\nAND\n" + other.toString());
	}

	/**
	 * 
	 * @return return[0] is for rows, and return[1] is for columns
	 */
	public Dimension[] getDimensions() {
		if (mDimension != null)
			return mDimension;

		int config_id = DBUtils
				.executeQueryForInt("SELECT config_id FROM symfony.mining_bi_cluster WHERE id="
						+ bicluster_id);
		ResultSet result = DBUtils
				.executeQuery("SELECT * FROM symfony.project_config WHERE id="
						+ config_id);

		mDimension = new Dimension[2];
		try {
			result.first();
			// Convention is that table a is for rows, and b is for columns
			mDimension[0] = new Dimension(result.getString("table_a"), result
					.getString("table_a_id_field"), result
					.getString("table_a_description_field"), true);
			mDimension[1] = new Dimension(result.getString("table_b"), result
					.getString("table_b_id_field"), result
					.getString("table_b_description_field"), true);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return mDimension;
	}

	public String[] getDimensionNames() {
		Dimension[] dims = getDimensions();
		return new String[] { dims[0].getName(), dims[1].getName() };
	}

	protected String getExternalDatabase() {
		if (externalDbName != null)
			return externalDbName;

		// Grab mining ID, use that to get Project ID, use that to get external
		// DB nameAndTable.
		// TODO if I'm fixing MINING_ID in DBUtils, should I use that here?
		int mining_id = DBUtils
				.executeQueryForInt("SELECT mining_id FROM symfony.mining_bi_cluster WHERE id="
						+ bicluster_id);
		int project_id = DBUtils
				.executeQueryForInt("SELECT project_id FROM symfony.mining WHERE id="
						+ mining_id);
		externalDbName = DBUtils
				.executeQueryForString("SELECT external_database FROM symfony.project WHERE id="
						+ project_id);
		return externalDbName;
	}

	public ArrayList<String> getValuesForDimension(String dimension) {
		Dimension target = null;
		Dimension[] dims = getDimensions();

		if (dims[0].getName().equals(dimension))
			target = dims[0];
		else if (dims[1].getName().equals(dimension))
			target = dims[1];
		else
			throw new IllegalArgumentException("Unknown dimension " + dimension);

		return target.getValues();
	}

	public class Dimension {
		private String nameAndTable;
		private String idField;
		private String valueField;
		private boolean isRow;
		private ArrayList<String> values; // delete-able

		// idField == the field in the table for this dimension
		// isRow == when looking for values, are we in the "row" or the "column"
		// table
		public Dimension(String name, String idField, String valueField,
				boolean isRow) {
			this.nameAndTable = name;
			this.idField = idField;
			this.valueField = valueField;
			this.isRow = isRow;
		}

		public String getName() {
			return nameAndTable;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("{nameAndTable=");
			sb.append(nameAndTable).append(',').append("idField=").append(
					idField).append(',').append("valueField=").append(
					valueField).append(',').append("isRow=").append(isRow)
					.append(',').append("values=").append(values).append('}');
			return sb.toString();
		}

		public ArrayList<String> getValues() {
			if (values != null)
				return values;

			String exDB = getExternalDatabase();
			String idTable = isRow ? "mining_bi_cluster_row"
					: "mining_bi_cluster_col";
			String selection = isRow ? "row_id" : "col_id";

			// Grab the row ID's of all our values from mining_bi_cluster_row
			ResultSet rs = DBUtils.executeQuery("SELECT " + selection
					+ " FROM symfony." + idTable + " WHERE bicluser_id="
					+ bicluster_id);

			StringBuffer idINlist = new StringBuffer();
			try {
				while (rs.next()) {
					idINlist.append(rs.getString(1));
					idINlist.append(',');
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			idINlist.setLength(idINlist.length() - 1);

			// Grab the values from the original table
			String query = "SELECT " + valueField + " FROM " + exDB + "."
					+ nameAndTable + " WHERE " + idField + " IN ("
					+ idINlist.toString() + ")";

			rs = DBUtils.executeQuery(query);
			ArrayList<String> result = new ArrayList<String>(
					idINlist.length() / 2 + 10);
			try {
				while (rs.next())
					result.add(rs.getString(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			values = result;

			return values;
		}
	}

}
