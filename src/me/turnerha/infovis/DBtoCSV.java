package me.turnerha.infovis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import me.turnerha.infovis.data.Bicluster;
import me.turnerha.infovis.data.Dimension;
import me.turnerha.infovis.data.Link;

/**
 * Takes the current Database and spits it out in the MineVis CSV format
 * 
 * @author hamiltont
 * 
 */
public class DBtoCSV {

	public DBtoCSV() {

		try {
			File mining = new File("mining.csv");
			FileWriter writer = new FileWriter(mining);
			writer.write("BiCluster Id, Row Type, Array of "
					+ "Rows, Column Type, Array of Columns\n");

			List<Bicluster> clusters = Bicluster.getAllBiclusters();
			for (Bicluster cluster : clusters) {
				writer.write(cluster.getBiclusterId() + ",");
				Dimension row = cluster.getRow(), col = cluster.getCol();
				writer.write(row.getName() + ",\"");
				for (String value : row.getValues())
					writer.write(value + ",");
				writer.write("\",");

				writer.write(col.getName() + ",\"");
				for (String value : col.getValues())
					writer.write(value + ",");
				writer.write("\"\n");
			}
			writer.write("\n");
			writer.close();

			// Build chain
			File chaining = new File("chaining.csv");
			writer = new FileWriter(chaining);

			writer.write("Link Id, Link Type, Source Id, Row "
					+ "Type, Source Rows, Column Type, Source "
					+ "Columns, Destination IdOk, Row Type, "
					+ "Destination Rows, Column Type, Destination "
					+ "Columns\n");

			for (Bicluster cluster : clusters) {

				Dimension row = cluster.getRow(), col = cluster.getCol();

				System.out.println("I am " + cluster.getBiclusterId() + ": "
						+ row.getName() + " <> " + col.getName());

				for (Link link : cluster.getAllLinks()) {
					if (link.isOverlapLink())
						continue;

					if (link.getTarget() != cluster)
						System.err.println("WTF");

					writer.write(",,"); // Ignore Id, Type

					// Source ID
					writer.write("" + cluster.getBiclusterId() + ",");
					// Row and Column type and values for Source
					writer.write(row.getName() + ",\"");
					for (String val : row.getValues())
						writer.write(val + ",");
					writer.write("\",");
					writer.write(col.getName() + ",\"");
					for (String val : col.getValues())
						writer.write(val + ",");
					writer.write("\",");

					Bicluster chd = link.getDestination();
					row = chd.getRow();
					col = chd.getCol();

					System.out.println("\tConnected with "
							+ chd.getBiclusterId() + ": " + row.getName()
							+ " <> " + col.getName() + " by " + link.getType());

					List<String> clusterRow = cluster.getRow().getValues();
					List<String> clusterCol = cluster.getCol().getValues();
					List<String> rowValues = row.getValues();
					List<String> colValues = col.getValues();
					int max = 0;
					if (clusterCol.size() > max)
						max = clusterCol.size();
					if (clusterRow.size() > max)
						max = clusterRow.size();
					if (rowValues.size() > max)
						max = rowValues.size();
					if (colValues.size() > max)
						max = colValues.size();

					for (int i = 0; i < max; i++) {
						String crow = "", ccol = "", lrow = "", lcol = "";
						if (i < clusterRow.size())
							crow = clusterRow.get(i);
						if (i < clusterCol.size())
							ccol = clusterCol.get(i);
						if (i < rowValues.size())
							lrow = rowValues.get(i);
						if (i < colValues.size())
							lcol = colValues.get(i);

						System.out.printf(
								"\t\t%-15s >< %-15s | %-15s >< %-15s\n", crow,
								ccol, lrow, lcol);
					}

					// Dest ID
					writer.write("" + chd.getBiclusterId() + ",");
					// Row and Column type and values for Source
					writer.write(row.getName() + ",\"");
					for (String val : row.getValues())
						writer.write(val + ",");
					writer.write("\",");
					writer.write(col.getName() + ",\"");
					for (String val : col.getValues())
						writer.write(val + ",");
					writer.write("\"");

					// Final endline
					writer.write("\n");
				}

				// for (Integer overlapped : cluster.getOverlappedBiclusters())
				// {

				// }

			}

			// Final endline
			writer.write("\n");
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new DBtoCSV();
	}

}
