package com.vi.counselingtoolsservice.port.out;

import com.vi.counselingtoolsservice.api.model.UserTools;
import org.springframework.data.repository.CrudRepository;

public interface UserToolsRepository extends CrudRepository<UserTools, String> {

}
