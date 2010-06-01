package models;

 
import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.UnitTest;
import util.TestHelper;

public class RepoTest extends UnitTest {
	
	@BeforeClass
	public static void init() {
		AppProps.DATA_FOLDER.mkdirs();
	}

	@Test 
	public void testRepoDefault() {
		String RID = "test-" + TestHelper.randomHashString();
		Repo repo = new Repo(RID);
		
		assertEquals( RID, repo.rid );
		assertEquals( RID, repo.getFile().getName() );
		
	}
	
	@Test 
	public void testRepoCreate() {
		String RID = "test-" + TestHelper.randomHashString();
		Repo repo = new Repo(RID,true);
		
		assertEquals( RID, repo.rid );
		assertEquals( RID, repo.getFile().getName() );
		assertTrue( repo.fFolder.exists() );
		assertTrue( repo.fMarker.exists() );
		assertFalse( repo.fLock.exists() );
		assertEquals( Status.INIT, repo.getStatus() );
		
	}
	
	@Test
	public void testCopy() {
		Repo repo = new Repo( TestHelper.randomHashString() );
		Repo copy = new Repo(repo);
		
		assertEquals( copy, repo );
		assertEquals( copy.hashCode(), repo.hashCode() );
	}

	@Test
	public void testExpiredStatus() {
		Repo repo = new Repo( TestHelper.randomHashString(), true );
		assertFalse( repo.isExpired() );

		repo.touch( System.currentTimeMillis() - (AppProps.instance().getRequestTimeToLive()+10)*1000 );
		assertTrue( repo.isExpired() );
		
	} 
	
	@Test
	public void testFindAll() {
		/* clean all folder */
		File[] files = AppProps.DATA_FOLDER.listFiles();
		for( File file : files ) if( file.isDirectory() ) {
			FileUtils.deleteQuietly(file);
		}

		/* now tests */
		Repo repo1 = new Repo("test-" + TestHelper.randomHashString(), true);
		Repo repo2 = new Repo("test-" + TestHelper.randomHashString(), true);
		Repo repo3 = new Repo("test-" + TestHelper.randomHashString(), false); // <-- does not crete it on fail system 
		
		List<Repo> repos = Repo.findByStatus(Status.INIT);
		assertTrue( repos.contains(repo1) );
		assertTrue( repos.contains(repo2) );
		assertFalse( repos.contains(repo3) );
	}
	
	@Test 
	public void testDrop() throws Exception {

		Repo repo = new Repo("test-drop",true);
		assertTrue( repo.fFolder.exists() );
		
		/*
		 * TODO improve this test locking a file a forcing the deletion
		 */
		repo.drop(true);
		assertFalse( repo.fFolder.exists() );
	}
	
}
