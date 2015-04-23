urlOpenSPIM = "http://openspim.org/update/";
urlMM = "http://sites.imagej.net/Micro-Manager-dev/";

importClass(Packages.ij.IJ);

var context = IJ.runPlugIn("org.scijava.Context", null);
var log = context.getService("org.scijava.log.LogService");

importClass(Packages.net.imagej.updater.FilesCollection);
importClass(Packages.java.io.File);
var files = new FilesCollection(log, new File(IJ.getDirectory("imagej")));
files.read();

urlOpenSPIM = "http://openspim.org/update/";
urlMM = "http://sites.imagej.net/Micro-Manager-dev/";
siteOpenSPIM = files.getUpdateSite("OpenSPIM", true);
hasOpenSPIM = siteOpenSPIM != null && siteOpenSPIM.isActive() && urlOpenSPIM.equals(siteOpenSPIM.getURL());
siteMM = files.getUpdateSite("Micro-Manager-dev", true);
hasMMNightly = siteMM != null && siteMM.isActive() && urlMM.equals(siteMM.getURL());
if (!hasMMNightly) {
	if (siteMM != null) {
		siteMM.setActive(true);
		siteMM.setLastModified(-1);
	} else {
		siteMM = files.addUpdateSite("Micro-Manager-dev", urlMM, null, null, -1);
	}
	if (siteOpenSPIM == null) {
		siteOpenSPIM = files.addUpdateSite("OpenSPIM", urlOpenSPIM, null, null, -1);
	} else if (!hasOpenSPIM) {
		siteOpenSPIM.setActive(true);
	}
	if (siteMM.compareTo(siteOpenSPIM) > 0) {
		updateSitesField = files.getClass().getDeclaredField("updateSites");
		updateSitesField.setAccessible(true);
		updateSites = updateSitesField.get(files);
		// OpenSPIM needs to be able to override Micro-Manager-dev
		updateSites.remove(siteOpenSPIM.getName());
		updateSites.put(siteOpenSPIM.getName(), siteOpenSPIM);
	}
	files.write();
	IJ.run("Update...");
}
