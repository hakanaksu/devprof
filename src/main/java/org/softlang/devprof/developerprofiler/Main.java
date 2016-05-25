package org.softlang.devprof.developerprofiler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.softlang.devprof.extractor.ApiExtractor;
import org.softlang.devprof.extractor.ApiUsageExtractor;
import org.softlang.devprof.extractor.GITRepositoryExtractor;
import org.softlang.devprof.extractor.MetricExtractor;
import org.softlang.devprof.extractor.MetricExtractor2;
import org.softlang.devprof.extractor.MetricExtractor3;
import org.softlang.devprof.helper.PackageModifier;
import org.softlang.devprof.persistence.entities.Domain;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.logic.PersistenceHandler;

public class Main {

	public static void main(String[] args) throws IOException {
		
        /****************************************************************/
        /**********************     Step 1.1    *************************/
        /****************************************************************/
		/*
		GITRepositoryExtractor gitRepositoryExtractor = new GITRepositoryExtractor("https://github.com/libgdx/libgdx.git", null, null);
		gitRepositoryExtractor.extractData();
		*/
        /****************************************************************/
        /**********************     Step 1.2    *************************/
        /****************************************************************/
		/*
		PackageModifier packageModifier = new PackageModifier();
		
		packageModifier.eliminationStatic();
		
		
		
		List<String> internalPackages = new LinkedList<>();
		internalPackages.add("com.badlogic");
		internalPackages.add("com.badlydrawngames");
		packageModifier.eliminiationInternal(internalPackages);
		
		
		
		Map<String,Boolean> map = new LinkedHashMap<>();
		//false - the package is not available
		//true - the package is available
		map.put("android.content", true);
		map.put("android.graphics", true);
		map.put("android.hardware", true);
		map.put("android.opengl", true);
		map.put("android.os", true);
		map.put("android.service", false);
		map.put("android.text", true);
		map.put("android.view", true);
		map.put("android.widget", true);
		map.put("aurelienribon", false);
		map.put("cli.MonoTouch", false);
		map.put("cli.OpenTK", true);
		map.put("cli.System", true);
		map.put("com.allen_sauer.gwt.voices.client", false);
		map.put("com.esotericsoftware", false);
		map.put("com.gargoylesoftware.htmlunit.javascript.host", false);
		map.put("com.google.gwt.animation.client", true);
		map.put("com.google.gwt.canvas", false);
		map.put("com.google.gwt.core", false);
		map.put("com.google.gwt.dom", false);
		map.put("com.google.gwt.event", false);
		map.put("com.google.gwt.typedarrays", false);
		map.put("com.google.gwt.user", false);
		map.put("com.google.gwt.xhr", false);
		map.put("com.jcraft", false);
		map.put("com.sun.jna", true);
		map.put("com.sun.opengl", false);
		map.put("de.matthiasmann.twl", true);
		map.put("de.matthiasmann.twlthemeeditor", false);
		map.put("gwt.g3d.client", true);
		map.put("japa.parser", true);
		map.put("java.awt", true);
		map.put("java.io", true);
		map.put("java.nio", true);
		map.put("java.security", true);
		map.put("java.util.concurrent", true);
		map.put("javax.crypto", true);
		map.put("javax.imageio", true);
		map.put("javax.media.opengl", true);
		map.put("javax.microedition.khronos", false);
		map.put("javax.sound.sampled", true);
		map.put("javax.swing", true);
		map.put("javax.xml", false);
		map.put("org.apache.http", true);
		map.put("org.jbox2d.collision", false);
		map.put("org.jbox2d.dynamics.contacts", false);
		map.put("org.junit", true);
		map.put("org.lwjgl", true);
		map.put("org.onepf.oms", true);
		map.put("org.robovm.apple", false);
		map.put("org.robovm.cocoatouch", false);
		map.put("org.robovm.objc", true);
		map.put("org.robovm.rt", true);
		map.put("org.xml.sax", true);
		map.put("javax.print.attribute", true);
		map.put("org.jbox2d.particle", false);
		map.put("sun.awt", false);
		map.put("sun.org.mozilla.javascript.internal", false);
		map.put("sun.nio.cs", false);
		map.put("org.omg", false);
		map.put("java.lang", false);
		
		packageModifier.eliminationSubfolder(map);
		 
		packageModifier.deletePackage("com.allen_sauer.gwt.voices.client"); 			//occurs 5 times
		packageModifier.deletePackage("com.android.vending.billing");					//occurs 7 times
		packageModifier.deletePackage("com.dozingcatsoftware.bouncy.util.MathUtils");	//occurs 2 times
		packageModifier.deletePackage("com.google.gwt.webgl.client.WebGLRenderingContext"); //occurs 3 times
		packageModifier.deletePackage("de.matthiasmann.twlthemeeditor");				//occurs 7 times
		packageModifier.deletePackage("gwt.g2d.client.util");							//occurs 7 times
		packageModifier.deletePackage("gwt.g3d.client");								//occurs 11 times
		packageModifier.deletePackage("sun.org.mozilla.javascript.internal"); 			//occurs 2 times
		
		packageModifier.replacePackageReferences("java.util.Map","java.util",true);				//part of subfolder elimination
		packageModifier.replacePackageReferences("java.util.ResourceBundle","java.util",true);	//part of subfolder elimination

		
		packageModifier.deletePackage("android.annotation"); 							//No API elements identified
		packageModifier.deletePackage("org.robovm.cocoatouch"); 							//occurs 112 times - The API doesn't exist anymore
		 
		packageModifier.deletePackage("cli.MonoTouch"); 							//API not found
		packageModifier.deletePackage("cli.objectal"); 								//API not found
		packageModifier.deletePackage("cli.OpenTK"); 								//API not found
		packageModifier.deletePackage("cli.System"); 								//API not found
		*/
        /****************************************************************/
        /**********************     Step 2.1    *************************/
        /****************************************************************/
		
		/*
		 * extract information of APIs
		 */
		/*
		ApiExtractor apiExtractor;
		File api;
		
		
		api = new File("apis/android.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		

		
		apiExtractor.extractData("android.app", true);
		apiExtractor.extractData("android.content", true);
		apiExtractor.extractData("android.graphics", true);
		apiExtractor.extractData("android.hardware", true);
		apiExtractor.extractData("android.media", true);
		apiExtractor.extractData("android.net", true);
		apiExtractor.extractData("android.opengl", true);
		apiExtractor.extractData("android.os", true);
		apiExtractor.extractData("android.provider", true);
		apiExtractor.extractData("android.service", true);
		apiExtractor.extractData("android.text", true);
		apiExtractor.extractData("android.util", true);
		apiExtractor.extractData("android.view", true);
		apiExtractor.extractData("android.widget", true);
		apiExtractor.extractData("javax.crypto", true);
		apiExtractor.extractData("javax.microedition.khronos", true);
		
		
		api = new File("apis/android-support-v4.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("android.support.v4.app", true);
		
		
		api = new File("apis/tween-engine-api.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("aurelienribon", true);
		
		
		api = new File("apis/in-app-purchasing-1.0.20.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.amazon.inapp.purchasing", true);

		
		api = new File("apis/tablelayout.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.esotericsoftware.tablelayout", true);
		
		api = new File("apis/gwt-dev-2.7.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.gargoylesoftware.htmlunit.javascript.host", true);
		

		api = new File("apis/gwt-user-2.7.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.google.gwt.animation.client", true);
		apiExtractor.extractData("com.google.gwt.canvas", true);
		apiExtractor.extractData("com.google.gwt.core", true);
		apiExtractor.extractData("com.google.gwt.dom", true);
		apiExtractor.extractData("com.google.gwt.event", true);
		apiExtractor.extractData("com.google.gwt.http.client", true);
		apiExtractor.extractData("com.google.gwt.i18n.client", true);
		apiExtractor.extractData("com.google.gwt.logging.client", true);
		apiExtractor.extractData("com.google.gwt.media.client", true);
		apiExtractor.extractData("com.google.gwt.regexp.shared", true);
		apiExtractor.extractData("com.google.gwt.storage.client", true);
		apiExtractor.extractData("com.google.gwt.typedarrays", true);
		apiExtractor.extractData("com.google.gwt.user", true);
		apiExtractor.extractData("com.google.gwt.xhr", true);
		
		
		api = new File("apis/jsch-0.1.53.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.jcraft", true);
		
		
		api = new File("apis/gluegen-rt-1.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.sun.gluegen.runtime", true);

		
		api = new File("apis/jna-4.1.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.sun.jna", true);

		
		api = new File("apis/jogl-1.1.1.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("com.sun.opengl", true);
		apiExtractor.extractData("javax.media.opengl", true);
		
		
		api = new File("apis/rt.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		apiExtractor.extractData("com.sun.org.apache.xalan.internal.xsltc", true);
		apiExtractor.extractData("com.sun.org.apache.xerces.internal.parsers", true);
		apiExtractor.extractData("java.applet", true);
		apiExtractor.extractData("java.awt", true);
		apiExtractor.extractData("java.io", true);
		apiExtractor.extractData("java.lang", true);
		apiExtractor.extractData("java.math", true);
		apiExtractor.extractData("java.net", true);
		apiExtractor.extractData("java.nio", true);
		apiExtractor.extractData("java.security", true);
		apiExtractor.extractData("java.text", true);
		apiExtractor.extractData("java.util", false);
		apiExtractor.extractData("java.util.concurrent", true);
		apiExtractor.extractData("java.util.prefs", true);
		apiExtractor.extractData("java.util.regex", true);
		apiExtractor.extractData("java.util.zip", true);
		apiExtractor.extractData("javax.imageio", true);
		apiExtractor.extractData("javax.print.attribute", true);
		
		
		apiExtractor.extractData("javax.sound.sampled", true);
		apiExtractor.extractData("javax.swing", true);
		apiExtractor.extractData("javax.xml", true);
		apiExtractor.extractData("org.omg", true);
		apiExtractor.extractData("org.w3c.dom", true);
		apiExtractor.extractData("org.xml.sax", true);
		apiExtractor.extractData("sun.awt", true);
		apiExtractor.extractData("sun.nio.cs", true);
		apiExtractor.extractData("sun.rmi.runtime", true);
		
		
		
		api = new File("apis/TWL-android.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("de.matthiasmann.twl", true);
		
		
		api = new File("apis/javaparser-1.0.8.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("japa.parser", true);
				

		api = new File("apis/jlayer-1.0.1.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("javazoom.jl.decoder", true);
		

		api = new File("apis/commons-io-2.4.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.apache.commons.io", true);
		
		
		api = new File("apis/httpcore-4.4.2.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.apache.http", true);

		api = new File("apis/jbox2d-lib.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.jbox2d.collision", true);
		apiExtractor.extractData("org.jbox2d.dynamics.contacts", true);
		apiExtractor.extractData("org.jbox2d.particle", true);
		

		api = new File("apis/json.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.json", true);
		

		api = new File("apis/junit-4.12.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.junit", true);
		

		api = new File("apis/lwjgl-2.9.3.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.lwjgl", true);
		

		api = new File("apis/openiab-0.9.7.1.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.onepf.oms", true);
		

		api = new File("apis/robovm-apple-1.7.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.robovm.apple", true);
		

		api = new File("apis/robovm-objc-1.7.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.robovm.objc", true);
		

		api = new File("apis/robovm-rt-1.7.0.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("org.robovm.rt", true);
		

		api = new File("apis/ouya-sdk.jar");
		apiExtractor = new ApiExtractor(new URL("file:"+api.getAbsolutePath()));
		
		apiExtractor.extractData("tv.ouya.console.api", true);
		*/
		
		
		/****************************************************************/
        /**********************     Step 2.2    *************************/
        /****************************************************************/
		/*
		Domain domain;
		
		domain = packageModifier.addDomain("GUI");
		String[] guiAPIs = {"android.graphics",
				"android.view",
				"android.widget",
				"com.esotericsoftware.tablelayout",
				"com.google.gwt.animation.client",
				"com.google.gwt.canvas",
				"com.google.gwt.event",
				"java.awt",
				"javax.swing",
				"sun.awt"};
		packageModifier.addApiToDomain(guiAPIs,domain);
		
		domain = packageModifier.addDomain("Media");
		String[] mediaAPIs = {"android.media",
				"android.opengl",
				"aurelienribon",
				"com.google.gwt.media.client",
				"com.sun.gluegen.runtime",
				"com.sun.opengl",
				"javax.media.opengl",
				"javax.microedition.khronos",
				"javax.sound.sampled",
				"javazoom.jl.decoder"};
		packageModifier.addApiToDomain(mediaAPIs, domain);
		
		domain = packageModifier.addDomain("Web");
		String[] webAPIs = {"android.net",
				"android.provider",
				"com.amazon.inapp.purchasing",
				"com.gargoylesoftware.htmlunit.javascript.host",
				"com.google.gwt.core",
				"com.google.gwt.http.client",
				"com.google.gwt.i18n.client",
				"com.google.gwt.user",
				"com.google.gwt.xhr",
				"com.jcraft",
				"java.net",
				"java.security",
				"javax.crypto",
				"org.apache.http"};
		packageModifier.addApiToDomain(webAPIs, domain);
		
		domain = packageModifier.addDomain("Meta");
		String[] metaAPIs = {"android.app",
				"android.os",
				"android.util",
				"com.google.gwt.typedarrays",
				"java.util"};
		packageModifier.addApiToDomain(metaAPIs, domain);
		
		domain = packageModifier.addDomain("Component");
		String[] componentAPIs = {"android.hardware",
				"android.support.v4.app",
				"java.applet",
				"org.jbox2d.collision",
				"org.jbox2d.dynamics.contacts",
				"org.jbox2d.particle",
				"org.omg"};
		packageModifier.addApiToDomain(componentAPIs, domain);
		
		domain = packageModifier.addDomain("Format");
		String[] formatAPIs = {"android.text",
				"java.text",
				"javax.imageio"};
		packageModifier.addApiToDomain(formatAPIs, domain);
		
		domain = packageModifier.addDomain("XML");
		String[] xmlAPIs = {"com.google.gwt.dom",
				"com.sun.org.apache.xalan.internal.xsltc",
				"javax.xml",
				"org.w3c.dom",
				"org.xml.sax"};
		packageModifier.addApiToDomain(xmlAPIs, domain);
		
		domain = packageModifier.addDomain("Logging");
		String[] loggingAPIs = {"com.google.gwt.logging.client",
				"sun.rmi.runtime"};
		packageModifier.addApiToDomain(loggingAPIs, domain);
		
		domain = packageModifier.addDomain("Parsing");
		String[] parsingAPIs = {"com.google.gwt.regexp.shared",
				"com.sun.org.apache.xerces.internal.parsers",
				"japa.parser",
				"java.util.regex",
				"org.json"};
		packageModifier.addApiToDomain(parsingAPIs, domain);
		
		domain = packageModifier.addDomain("Archiving");
		String[] archivingAPIs = {"com.google.gwt.storage.client",
				"java.util.zip"};
		packageModifier.addApiToDomain(archivingAPIs, domain);
		
		domain = packageModifier.addDomain("CrossPlatform");
		String[] crossPlatformgAPIs = {"com.sun.jna",
				"de.matthiasmann.twl",
				"org.lwjgl",
				"org.onepf.oms",
				"org.robovm.apple",
				"org.robovm.objc",
				"org.robovm.rt",
				"tv.ouya.console.api"};
		packageModifier.addApiToDomain(crossPlatformgAPIs, domain);
		
		domain = packageModifier.addDomain("Basics");
		String[] basicsAPIs = {"java.lang"};
		packageModifier.addApiToDomain(basicsAPIs, domain);
		
		
		domain = packageModifier.addDomain("Concurrency");
		String[] concurrencyAPIs = {"android.service",
				"java.util.concurrent"};
		packageModifier.addApiToDomain(concurrencyAPIs, domain);
		
		domain = packageModifier.addDomain("IO");
		String[] ioAPIs = {"java.io",
				"java.nio",
				"javax.print.attribute",
				"org.apache.commons.io",
				"sun.nio.cs"};
		packageModifier.addApiToDomain(ioAPIs, domain);
		
		domain = packageModifier.addDomain("Math");
		String[] mathAPIs = {"java.math"};
		packageModifier.addApiToDomain(mathAPIs, domain);
		
		domain = packageModifier.addDomain("Configuration");
		String[] configurationAPIs = {"android.content",
				"java.util.prefs"};
		packageModifier.addApiToDomain(configurationAPIs, domain);
		
		domain = packageModifier.addDomain("Testing");
		String[] testingnAPIs = {"org.junit"};
		packageModifier.addApiToDomain(testingnAPIs, domain);
		
		*/
		/****************************************************************/
        /**********************     Step 3.1    *************************/
        /****************************************************************/
		
		int[] unqualifiedRevisions = {25,54,110,111,451,453,648,1724,2195,2197,2526,3011,3221,3315,3511,3556,3735,3736,4955,4117,4656,4657,4757,4832,4854,4955,5713,5739,5743,6451,6868,7329,7338,7363,7985,8389,8467,8531,8945,9214};
		/*
		ApiUsageExtractor apiUsageExtractor = new ApiUsageExtractor();
		apiUsageExtractor.extractData(0, 5000, unqualifiedRevisions);
		apiUsageExtractor.extractData(5001, 50000, unqualifiedRevisions);
		apiUsageExtractor.extractData(50001, 60000, unqualifiedRevisions);
		apiUsageExtractor.extractData(60001, 75000, unqualifiedRevisions);
		apiUsageExtractor.extractData(75001, 90000, unqualifiedRevisions);
		apiUsageExtractor.extractData(90001, 120000, unqualifiedRevisions);
		apiUsageExtractor.extractData(120001, 150000, unqualifiedRevisions);
		apiUsageExtractor.extractData(150001, 200000, unqualifiedRevisions);
		apiUsageExtractor.extractData(200001, 250000, unqualifiedRevisions);
		apiUsageExtractor.extractData(250001, 300000, unqualifiedRevisions);
		apiUsageExtractor.extractData(300001, 310000, unqualifiedRevisions);
		apiUsageExtractor.extractData(310001, 320000, unqualifiedRevisions);
		apiUsageExtractor.extractData(320001, 330000, unqualifiedRevisions);
		apiUsageExtractor.extractData(330001, 390000, unqualifiedRevisions);
		apiUsageExtractor.extractData(390001, 450000, unqualifiedRevisions);
		apiUsageExtractor.extractData(450001, 500000, unqualifiedRevisions);
		apiUsageExtractor.extractData(500001, 520000, unqualifiedRevisions);
		apiUsageExtractor.extractData(520001, 550000, unqualifiedRevisions);
		apiUsageExtractor.extractData(550001, 600000, unqualifiedRevisions);
		apiUsageExtractor.extractData(600001, 650000, unqualifiedRevisions);
		apiUsageExtractor.extractData(650001, 700000, unqualifiedRevisions);
		apiUsageExtractor.extractData(700001, 750000, unqualifiedRevisions);
		apiUsageExtractor.extractData(750001, 800000, unqualifiedRevisions);
		apiUsageExtractor.extractData(800001, 850000, unqualifiedRevisions);
		apiUsageExtractor.extractData(850001, 900000, unqualifiedRevisions);
		apiUsageExtractor.extractData(900001, 950000, unqualifiedRevisions);
		apiUsageExtractor.extractData(950001, 1000000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1000001, 1050000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1050001, 1100000, unqualifiedRevisions);

		apiUsageExtractor.extractData(1100001, 1150000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1150001, 1200000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1200001, 1250000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1250001, 1300000, unqualifiedRevisions);
		apiUsageExtractor.extractData(1300001, 1350000, unqualifiedRevisions);
		*/
		/****************************************************************/
        /**********************     Step 3.2    *************************/
        /****************************************************************/
		
		MetricExtractor3 metricExtractor = new MetricExtractor3(unqualifiedRevisions);
		metricExtractor.extractData();
		
	}

}
