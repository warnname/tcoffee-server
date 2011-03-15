package plugins;

import play.Logger;
import play.PlayPlugin;
import play.cache.Cache;
import play.jobs.Job;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.vfs.VirtualFile;
import bot.BotListener;
import bot.BotListener.Config;

public class BootstrapPlugin extends PlayPlugin {

	Boolean terminated = false;
	
	/**
	 * Append the /conf path a location containing page templates
	 */
	@Override
	public void onApplicationStart() { 
	//	Play.templatesPath.add( VirtualFile.open(Play.applicationPath).child("conf") );
	}
	
	/**
	 * Notify application starts event
	 */
	@Override
    public void afterApplicationStart() {
		
		/* add current timestamp */
		Cache.set("server-start-time", System.currentTimeMillis());
		
		
		/* 
		 * installing a mail listener if it is configured 
		 */
		startMailListener();
	}

	private void startMailListener() {

		Config config = BotListener.getConfig();
		if( config!=null && config.isActive() ) { 
			Logger.info(">>> Installing mail listener");

			final BotListener receiver = new BotListener() {
				public boolean isTerminated() { return terminated; }
			};
			
			Job starter = new Job() {
				public void doJob() throws Exception { receiver.run(); };
			};
			
			starter.in(config.delay);
			
		}
		else { 
			Logger.info(">>> Mail listener service NOT configured");
		}


	}

	
	/**
	 * Notify application stops 
	 */
	@Override
    public void onApplicationStop() {
		Logger.info(">>> Stopping server");
		terminated = true;
	}

	
	/**
	 * Load "/conf" provided templates
	 */
	@Override
    public Template loadTemplate(VirtualFile file) {

		/* 
		 * interpect templates in the /conf path 
		 */
		if( !file.relativePath().startsWith("/conf/") ) { 
			return null;
		}

		Logger.debug("Laoading template: '%s'",  file.relativePath() );
        String key = (file.relativePath().hashCode() + "").replace("-", "M");
        return TemplateLoader.load( key, file.contentAsString() );
    }

	
	
}
