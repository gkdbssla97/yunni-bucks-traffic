package sejong.coffee.yun.repository.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.repository.user.jpa.JpaUserRepository;

import java.util.List;

import static sejong.coffee.yun.domain.exception.ExceptionControl.*;

@Repository
@Primary
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    @Transactional
    public Member save(Member member) {
        return jpaUserRepository.save(member);
    }

    @Override
    public Member findById(Long id) {
        return getUser(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public List<Member> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jpaUserRepository.delete(getUser(id));
    }

    @Override
    public boolean exist(Long id) {
        return jpaUserRepository.existsById(id);
    }

    @Override
    @Transactional
    public void updateName(Long id, String name) {
        Member member = getUser(id);

        member.updateName(name);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, String password) {
        Member member = getUser(id);

        member.updatePassword(password);
    }

    @Override
    @Transactional
    public void updateEmail(Long id, String email) {
        Member member = getUser(id);

        member.updateEmail(email);
    }

    @Override
    public void duplicateEmail(String email) {
        if(jpaUserRepository.existsByEmail(email)) {
            throw DUPLICATE_USER_EMAIL.duplicatedException();
        }
    }

    @Override
    public void duplicateName(String name) {
        if(jpaUserRepository.existsByName(name)) {
            throw DUPLICATE_USER_NAME.duplicatedException();
        }
    }

    private Member getUser(Long id) {
        return jpaUserRepository.findById(id)
                .orElseThrow(NOT_FOUND_USER::notFoundException);
    }

    @Override
    public void clear() {
        jpaUserRepository.deleteAll();
    }
}
