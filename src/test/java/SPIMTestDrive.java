import java.io.File;

/**
 * Created by moon on 4/20/15.
 */
public class SPIMTestDrive
{
	public static void main(String[] args)
	{
		if (!new File("mmplugins").isDirectory()) {
			throw new RuntimeException("The working directory must point to an existing Micro-Manager directory!");
		}
		MMStudioPlugin mm = new MMStudioPlugin();

		mm.run( "" );
	}
}
