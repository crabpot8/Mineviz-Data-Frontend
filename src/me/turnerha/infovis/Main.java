package me.turnerha.infovis;

import java.util.List;

import me.turnerha.infovis.data.Bicluster;
import me.turnerha.infovis.data.Cache;
import me.turnerha.infovis.data.DBUtils;

public class Main {

	public static void main(String args[]) {
		// String dbtime;
		// String query = "Select * FROM symfony.project";
		//
		// ResultSet rs = DBUtils.executeQuery(query);
		//
		// try {
		// while (rs.next()) {
		// dbtime = rs.getString(2);
		// System.out.println(dbtime);
		// }
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }

		/*Bicluster b = DBUtils.listBiclusters(1).get(0);
	
		List<Integer> chains = b.getChainedBiclusters();
		for (Integer i : chains) {
			Bicluster other = Cache.getBicluster(i);
			String[] bnames = b.getDimensionNames();
			String[] onames = other.getDimensionNames();
			
			System.out.println(bnames[0] + " x " + bnames[1] + " && "+ onames[0] + " x " + onames[1]);
			System.out.println("\tShare: " + b.findCommonDimension(other));
		}
			
		
		DBUtils.close();
		*/
	}

}
