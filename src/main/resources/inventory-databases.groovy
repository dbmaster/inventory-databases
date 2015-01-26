import java.util.ArrayList
import java.util.Iterator
import java.util.List
import java.util.Map.Entry

import com.branegy.inventory.api.InventoryService
import com.branegy.inventory.model.Database
import com.branegy.service.core.QueryRequest
import com.branegy.inventory.model.DatabaseUsage
import com.branegy.inventory.model.Application
import com.branegy.inventory.model.ContactLink
import com.branegy.inventory.model.Contact
import com.branegy.inventory.api.ContactLinkService
import com.branegy.service.base.api.ProjectService
import com.branegy.cfg.IPropertySupplier


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
 
db_fields = p_db_fields == null ? [] : p_db_fields.split("[;,]")
app_fields = p_app_fields == null ? [] : p_app_fields.split("[;,]")
con_fields = p_con_fields == null ? [] : p_con_fields.split("[;,]")

println "Total number of databases is ${inventoryDBs.size()} <br/>"

println """<table class="simple-table" cellspacing="0" cellpadding="10">
           <tr style="background-color:#EEE">
             <td>Server</td>
             <td>Database</td>
             ${db_fields.collect { "<td>${it}</td>" }.join("")}
             <td>Application</td>
             ${app_fields.collect { "<td>${it}</td>" }.join("")}
             <td>Role</td>
             <td>Contact</td>
             ${con_fields.collect { "<td>${it}</td>" }.join("")}
           </tr>"""

def globalProperties = dbm.getService(IPropertySupplier.class)
def roleField = globalProperties.getProperty("contract_role.role.field_name","ContactRole")
logger.debug("Will be using field ${roleField}")


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
        for (ContactLink contactLink:contactLinkList) {
            println "<tr>"
            println """<td><a href="#inventory/project:${toURL(projectName)}/servers/server:${toURL(database.getServerName())}">${database.getServerName()}</a></td>"""
            println """<td><a href="#inventory/project:${toURL(projectName)}/databases/server:${toURL(database.getServerName())},db:${toURL(database.getDatabaseName())}">${database.getDatabaseName()}</a></td></td>"""
       
            db_fields.each { fieldName -> println "<td>${emptystr(database.getCustomData(fieldName))}</td>" }
            println "<td>"
            
            if (app!=null){
                println """<a href="#inventory/project:${toURL(projectName)}/applications/application:${toURL(app.getApplicationName())}">${app.getApplicationName()}</a>""";
                app_fields.each { fieldName -> println "<td>${emptystr(app.getCustomData(fieldName))}</td>" }
            }
            println "</td>"
       
            if (contactLink!=null){
                println "<td>${contactLink.getCustomData(roleField)}</td>"
                def contact = contactLink.getContact()
                println """<td><a href="#inventory/project:${toURL(projectName)}/contacts/contact:${contact.getContactName()}">${contact.getContactName()}</a></td>"""
                con_fields.each { fieldName -> println "<td>${emptystr(contact.getCustomData(fieldName))}</td>" }
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