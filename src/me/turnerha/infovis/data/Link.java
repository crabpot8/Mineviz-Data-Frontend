package me.turnerha.infovis.data;

// TODO verify link e.g check if the type exists in both target and destination
public class Link {
	private Integer targetBicluster;
	private Integer destinationBicluster;
	private int chaining_link_id;
	private String type; // delete-able

	protected Link(Integer targetBicluster, Integer destinationBicluster,
			int chaining_link_id) {
		this.targetBicluster = targetBicluster;
		this.destinationBicluster = destinationBicluster;
		this.chaining_link_id = chaining_link_id;
	}

	public Bicluster getTarget() {
		return Cache.getBicluster(targetBicluster);
	}

	public Bicluster getDestination() {
		return Cache.getBicluster(destinationBicluster);
	}

	public String getType() {
		if (type != null)
			return type;

		// Find Type ID
		String query = "SELECT chaining_link_type_id FROM symfony.chaining_link c WHERE c.id="
				+ chaining_link_id;
		int typeId = DBUtils.executeQueryForInt(query);

		// Find Type Name
		String nameQuery = "SELECT name FROM symfony.chaining_link_type c WHERE c.id="
				+ typeId;
		type = DBUtils.executeQueryForString(nameQuery);
		return type;
	}

	public boolean isOverlapLink() {
		Bicluster target = getTarget();
		Bicluster dest = getDestination();
		if (target.getRow().getName().equals(dest.getRow().getName())
				&& (target.getCol().getName().equals(dest.getCol().getName())))
			return true;
		return false;
	}

	public boolean isConnectionLink() {
		return !isOverlapLink();
	}
}
