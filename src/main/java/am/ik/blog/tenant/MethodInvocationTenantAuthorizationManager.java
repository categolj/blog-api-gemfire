package am.ik.blog.tenant;

import am.ik.blog.security.Authorized;
import am.ik.blog.security.Privilege;
import am.ik.blog.util.Tuple2;
import am.ik.blog.util.Tuples;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.parameters.P;
import org.springframework.util.ReflectionUtils;

public class MethodInvocationTenantAuthorizationManager extends AbstractTenantAuthorizationManager<MethodInvocation> {

	@Override
	protected boolean isPermitted(@Nullable String tenantId, @Nullable String resource, Set<Privilege> privileges) {
		if (tenantId != null) {
			return false;
		}
		if (!"entry".equals(resource)) {
			return false;
		}
		int size = privileges.size();
		if (size == 1) {
			return privileges.contains(Privilege.LIST) || privileges.contains(Privilege.GET);
		}
		else if (size == 2) {
			return privileges.contains(Privilege.LIST) && privileges.contains(Privilege.GET);
		}
		return false;
	}

	@Override
	@Nullable protected String tenantId(MethodInvocation context) {
		Parameter[] parameters = context.getMethod().getParameters();
		int i;
		for (i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			P p = parameter.getAnnotation(P.class);
			if (p != null && "tenantId".equals(p.value())) {
				break;
			}
		}
		if (i < parameters.length) {
			Object argument = context.getArguments()[i];
			if (argument instanceof String) {
				return (String) argument;
			}
			else if (argument != null) {
				Method tenantIdMethod = ReflectionUtils.findMethod(argument.getClass(), "tenantId");
				if (tenantIdMethod != null && String.class.isAssignableFrom(tenantIdMethod.getReturnType())) {
					return (String) ReflectionUtils.invokeMethod(tenantIdMethod, argument);
				}
			}
		}
		return null;
	}

	@Override
	@Nullable protected Tuple2<String, Set<Privilege>> resourceAndPrivileges(MethodInvocation context) {
		Authorized authorized = context.getMethod().getAnnotation(Authorized.class);
		if (authorized != null) {
			return Tuples.of(authorized.resource(), Set.of(authorized.requiredPrivileges()));
		}
		return null;
	}

}