import io.dbmaster.testng.BaseToolTestNGCase;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test

import com.branegy.tools.api.ExportType;


public class InventoryDatabasesIT extends BaseToolTestNGCase {

    @Test
    public void test() {
        def parameters = [ : ]
        String result = tools.toolExecutor("inventory-databases", parameters).execute()
        assertTrue(result.contains("Server"), "Unexpected search results ${result}");
    }
}
