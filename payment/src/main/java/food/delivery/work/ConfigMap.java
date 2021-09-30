package food.delivery.work;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "systeminfo")
public class ConfigMap {
	private String servertype;
	private String serveruser;
	
	public String getServertype() {
		return servertype;
	}
	public void setServertype(String servertype) {
		this.servertype = servertype;
	}
	public String getServeruser() {
		return serveruser;
	}
	public void setServeruser(String serveruser) {
		this.serveruser = serveruser;
	}
	
	

}
