package dp.weather.bulletin.configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApplicationUser applicationUser = loadApplicationUserByUsername(username);
        Collection<GrantedAuthority> authorities = (username.toLowerCase().startsWith("admin"))
                ? AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
                : AuthorityUtils.createAuthorityList("ROLE_USER");
        return new User(applicationUser.getUsername(), applicationUser.getPassword(), authorities);
    }

    public ApplicationUser loadApplicationUserByUsername(String username){
        return new ApplicationUser(username, "no-password");
    }


}
