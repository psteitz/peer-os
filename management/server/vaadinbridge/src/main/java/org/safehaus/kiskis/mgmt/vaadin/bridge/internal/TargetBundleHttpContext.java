package org.safehaus.kiskis.mgmt.vaadin.bridge.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

class TargetBundleHttpContext implements HttpContext {

    private final BundleContext callerContext;
    private final String importedPkgName;

    private Bundle targetBundle = null;

    public TargetBundleHttpContext(BundleContext callerContext, String importedPkgName) {
        this.callerContext = callerContext;
        this.importedPkgName = importedPkgName;
    }

    private Bundle getTargetBundle() {
        // Return target bundle immediately if it's non-null and still installed
        if (targetBundle != null && targetBundle.getState() != Bundle.UNINSTALLED) {
            return targetBundle;
        }
        targetBundle = null;

        // Get exported packages matching the specified name
        ServiceReference ref = callerContext.getServiceReference(PackageAdmin.class.getName());
        if (ref != null) {
            PackageAdmin pkgAdmin = (PackageAdmin) callerContext.getService(ref);
            if (pkgAdmin != null) {
                try {
                    ExportedPackage[] exportedPackages = pkgAdmin.getExportedPackages(importedPkgName);
                    // Find the one that's imported by the calling bundle
                    if (exportedPackages != null) {
                        outer:
                        for (ExportedPackage exportedPackage : exportedPackages) {
                            Bundle[] importingBundles = exportedPackage.getImportingBundles();
                            for (Bundle bundle : importingBundles) {
                                if (bundle.getBundleId() == callerContext.getBundle().getBundleId()) {
                                    targetBundle = exportedPackage.getExportingBundle();
                                    break outer;
                                }
                            }
                        }
                    }
                } finally {
                    callerContext.ungetService(ref);
                }
            }
        }
        return targetBundle;
    }

    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    public URL getResource(String name) {
        Bundle bundle = getTargetBundle();
        return bundle != null ? bundle.getResource(name) : null;
    }

    public String getMimeType(String name) {
        return null;
    }
}