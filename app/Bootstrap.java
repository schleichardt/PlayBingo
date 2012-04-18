import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
        if (Play.id.equals("test")) {
            Fixtures.deleteAllModels();
            Fixtures.loadModels("initial-data.yml");
        }
    }
}