package com.group11.flossy.repository;

import com.group11.flossy.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>  {
}
