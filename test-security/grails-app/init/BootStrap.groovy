import org.springframework.beans.factory.annotation.Autowired
import test.security.TestDataService

class BootStrap {
    @Autowired
    TestDataService testDataService

    def init = { servletContext ->
        println ">>>>>>>> Bootstrap init"
        testDataService.enterInitialData()
    }
    def destroy = {
    }
}
