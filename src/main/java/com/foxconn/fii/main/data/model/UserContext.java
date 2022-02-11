package com.foxconn.fii.main.data.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class UserContext extends User {

    private static final long serialVersionUID = -4865902302734595324L;

    private com.foxconn.fii.main.data.entity.User user;

    public com.foxconn.fii.main.data.entity.User getUser() {
        return user;
    }

    public UserContext(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public UserContext(String username, String password, Collection<? extends GrantedAuthority> authorities, com.foxconn.fii.main.data.entity.User user) {
        super(username, password, authorities);
        this.user = user;
    }

    public UserContext(String username, String password, boolean enabled, boolean accountNonExpired,
                       boolean credentialsNonExpired, boolean accountNonLocked,
                       Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

}
