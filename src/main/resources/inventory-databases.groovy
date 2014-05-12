import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.branegy.inventory.api.InventoryService;
import com.branegy.inventory.model.Database;
import com.branegy.service.core.QueryRequest;

def emptystr(obj) {
 return obj==null ? "" : obj;
}

InventoryService inventorySrv = dbm.getService(InventoryService.class);

inventoryDBs = new ArrayList(inventorySrv.getDatabaseList(new QueryRequest(p_query)));

inventoryDBs.sort { it.getServerName()+"_"+it.getDatabaseName()  }

def db2AppsLinks = inventorySrv.getDBUsageList();
dbApps = db2AppsLinks.groupBy { it.getDatabase() }

fields = p_fields == null ? [] : p_fields.split(";")


println "Total number of databases is ${inventoryDBs.size()} <br/>"

println """<table class="simple-table" cellspacing="0" cellpadding="10">
           <tr style="background-color:#EEE">
             <td>Server</td>
             <td>Database</td>
             ${fields.collect { "<td>${it}</td>" }.join("")}
             <td>Applications</td>
           </tr>"""

for (Database database: inventoryDBs) {
     println "<tr>"
     println "<td>${database.getServerName()}</td>"
     println "<td>${database.getDatabaseName()}</td>"

     fields.each { fieldName -> println "<td>${emptystr(database.getCustomData(fieldName))}</td>" }
     
     println "<td>"

     def apps = dbApps[database];
     if (apps!=null) {
         println apps.collect { it.getApplication()?.getApplicationName() }.join(", ")
     }

     println "</tr>"

}

println "</table>"