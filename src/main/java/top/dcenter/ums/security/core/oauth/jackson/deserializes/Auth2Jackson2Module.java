package top.dcenter.ums.security.core.oauth.jackson.deserializes;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import top.dcenter.ums.security.core.oauth.token.Auth2AuthenticationToken;
import top.dcenter.ums.security.core.oauth.userdetails.TemporaryUser;

/**
 * Auth2 Jackson2 Module
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/28 10:58
 */
public class Auth2Jackson2Module extends SimpleModule {

	public Auth2Jackson2Module() {
		super(Auth2Jackson2Module.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		SecurityJackson2Modules.enableDefaultTyping(context.getOwner());
	    context.setMixInAnnotations(Auth2AuthenticationToken.class,
                                    Auth2AuthenticationTokenJsonDeserializer.Auth2AuthenticationTokenMixin.class);
		context.setMixInAnnotations(RememberMeAuthenticationToken.class,
		                            RememberMeAuthenticationTokenJsonDeserializer.RememberMeAuthenticationTokenMixin.class);
		context.setMixInAnnotations(AnonymousAuthenticationToken.class,
		                            AnonymousAuthenticationTokenJsonDeserializer.AnonymousAuthenticationTokenMixin.class);
		context.setMixInAnnotations(User.class,
		                            UserDeserializer.UserMixin.class);
		context.setMixInAnnotations(TemporaryUser.class,
		                            TemporaryUserDeserializer.TemporaryUserMixin.class);
		context.setMixInAnnotations(WebAuthenticationDetails.class,
		                            WebAuthenticationDetailsDeserializer.WebAuthenticationDetailsMixin.class);
		context.setMixInAnnotations(AuthUser.class,
		                            AuthUserJsonDeserializer.AuthUserMixin.class);
		context.setMixInAnnotations(AuthToken.class,
		                            AuthUserJsonDeserializer.AuthTokenMixin.class);
	}
}