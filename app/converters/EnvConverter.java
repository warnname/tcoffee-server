package converters;

import models.Env;

/**
 * XStream converter for {@link Env}
 * 
 * @author Paolo Di Tommaso
 *
 */
public class EnvConverter extends AnyAttributeConverter<Env>{

	public EnvConverter() { 
		super(Env.class);
	}

}
