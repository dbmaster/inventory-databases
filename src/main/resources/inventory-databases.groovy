import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.branegy.inventory.api.InventoryService;
import com.branegy.inventory.model.Database;
import com.branegy.service.core.QueryRequest;
import com.branegy.inventory.model.DatabaseUsage;
import com.branegy.inventory.model.Application;
import com.branegy.inventory.model.ContactLink;
import com.branegy.inventory.model.Contact;
import com.branegy.inventory.api.ContactLinkService;
import com.branegy.service.base.api.ProjectService;

def emptystr(obj) {
 return obj==null ? "" : obj;
}

def toURL = { link -> link.encodeURL().replaceAll("\\+", "%20") }
String.metaClass.encodeURL = { java.net.URLEncoder.encode(delegate) }


InventoryService inventorySrv = dbm.getService(InventoryService.class);

inventoryDBs = new ArrayList(inventorySrv.getDatabaseList(new QueryRequest(p_query)));

inventoryDBs.sort { it.getServerName()+"_"+it.getDatabaseName()  }

String projectName =  dbm.getService(ProjectService.class).getCurrentProject().getName();

def db2AppsLinks = inventorySrv.getDBUsageList();
def dbApps = db2AppsLinks.groupBy { it.getDatabase() }
def contactLinks = dbm.getService(ContactLinkService.class).findAllByClass(Application.class,null)
def appId2contactLink = contactLinks.groupBy{ contactLink-> contactLink.getApplication().getId()};
 
fields = p_fields == null ? [] : p_fields.split("[;,]")

println "Total number of databases is ${inventoryDBs.size()} <br/>"

println """<table class="simple-table" cellspacing="0" cellpadding="10">
           <tr style="background-color:#EEE">
             <td>Server</td>
             <td>Database</td>
             ${fields.collect { "<td>${it}</td>" }.join("")}
             <td>Applications</td>
             <td>Role</td>
             <td>Contact</td>
             <td>Contact Email</td>
           </tr>"""

for (Database database: inventoryDBs) {
    def apps = dbApps[database];
    if (apps == null || apps.isEmpty()){
        apps = Collections.singletonList(null);
    }    
    for (DatabaseUsage dbusage: apps){
        def app = dbusage!=null?dbusage.getApplication():null;
        def contactLinkList = app!=null ? appId2contactLink.get(app.getId()): null;
        if (contactLinkList == null || contactLinkList.isEmpty()){
            contactLinkList = Collections.singletonList(null); 
        }
        for (ContactLink contactLink:contactLinkList){
            println "<tr>"
            println """<td><a href="#inventory/project:${toURL(projectName)}/servers/server:${toURL(database.getServerName())}">${database.getServerName()}</a></td>"""
            println """<td><a href="#inventory/project:${toURL(projectName)}/databases/server:${toURL(database.getServerName())},db:${toURL(database.getDatabaseName())}">${database.getDatabaseName()}</a></td></td>"""
       
            fields.each { fieldName -> println "<td>${emptystr(database.getCustomData(fieldName))}</td>" }
            println "<td>"
            
            if (app!=null){
                println """<a href="#inventory/project:${toURL(projectName)}/applications/application:${toURL(app.getApplicationName())}">${app.getApplicationName()}</a>""";
            }
            println "</td>"
       
            if (contactLink!=null){
                println "<td>${contactLink.getCustomData("ContactRole")}</td>"
                println """<td><a href="#inventory/project:${toURL(projectName)}/applications/application:${toURL(app.getApplicationName())}/contacts">${contactLink.getContact().getContactName()}</a></td>"""
                    println "<td>${emptystr(contactLink.getContact().getCustomData(Contact.EMAIL))}</td>"
            } else {
                println "<td></td>"
                println "<td></td>"
                println "<td></td>"
            }
            println "</tr>"
        }
    }
}

println "</table>"