package me.turnerha.infovis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import me.turnerha.infovis.data.Bicluster;
import me.turnerha.infovis.data.Cache;
import me.turnerha.infovis.data.Bicluster.Dimension;

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
				Dimension[] dims = cluster.getDimensions();
				Dimension row = dims[0], col = dims[1];
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
				Dimension[] dims = cluster.getDimensions();
				Dimension row = dims[0], col = dims[1];

				for (Integer chained : cluster.getChainedBiclusters()) {
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

					Bicluster chd = Cache.getBicluster(chained);
					Dimension[] dims2 = chd.getDimensions();
					row = dims2[0];
					col = dims2[1];
					// Dest ID
					writer.write("" + chained + ",");
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
