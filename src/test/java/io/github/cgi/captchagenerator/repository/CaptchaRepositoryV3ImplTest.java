package io.github.cgi.captchagenerator.repository;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class CaptchaRepositoryV3ImplTest {

    private RedisTemplate<UUID, CaptchaInfo> template;

    private ValueOperations<UUID, CaptchaInfo> valueOperations;

    private UUID captchaUuidInMock;
    private CaptchaInfo captchaInfoInMock;
    private long captchaTtlInMock;
    private TimeUnit captchaUnitInMock;

    private CaptchaRepositoryV3Impl repository;

    @BeforeEach
    public void setUp(){
        repository = new CaptchaRepositoryV3Impl();

        repository.setTtl(123L);
        repository.setUnit(TimeUnit.SECONDS);

        // Чтобы убрать предупреждение компилятора
        @SuppressWarnings("unchecked")
        ValueOperations<UUID, CaptchaInfo> valueOperations1 = Mockito.mock(ValueOperations.class);

        valueOperations = valueOperations1;
        when(valueOperations.get( any(UUID.class) ) )
                .thenReturn(captchaInfoInMock);

        Mockito.doAnswer((Answer<Void>) invocation -> {
            captchaUuidInMock = invocation.getArgument(0);
            captchaInfoInMock = invocation.getArgument(1);
            captchaTtlInMock = invocation.getArgument(2);
            captchaUnitInMock = invocation.getArgument(3);
            return null;
        }).when(valueOperations).set( any(UUID.class), any(CaptchaInfo.class), anyLong(), any(TimeUnit.class) );

        @SuppressWarnings("unchecked")
        RedisTemplate<UUID, CaptchaInfo> template1 = Mockito.mock(RedisTemplate.class);

        template = template1;
        when(template.opsForValue()).thenReturn(valueOperations);

        repository.setRedisTemplate(template);

    }


    @Test
    void saveResponseTest() {
        CaptchaInfo info = new CaptchaInfo(
                new byte[] { 11, 12, 13},
                "testCode",
                new Date(123123123L),
                CaptchaInfo.Status.NEW);
        CaptchaContext context = repository.save(info);

        assertNotNull(context, "Ответ должен возвращаться");
        assertNotNull(context.getUuid(), "uuid должен возвращаться");
        assertSame(info, context.getInfo(), "Оригинальный объект не возвращается в ответе");
        assertEquals(123, context.getTtl(), "TTL отличается от настроенного");
        assertEquals(TimeUnit.SECONDS, context.getTimeUnit(), "размерность отличается от настроенного");
    }

     @Test
    void saveCallsRedisSet() {
        CaptchaInfo info = new CaptchaInfo(
                new byte[] { 11, 12, 13},
                "testCode",
                new Date(123123123L),
                CaptchaInfo.Status.NEW);
        CaptchaContext context = repository.save(info);

        // Передаваемые значения
        assertEquals( context.getUuid(), captchaUuidInMock, "Вызов Redis выполнен с другим UUID" );
        assertSame( info, captchaInfoInMock, "Вызов Redis выполнен с другим CaptchaInfo" );
        assertSame( 123L, captchaTtlInMock, "Вызов Redis выполнен с другим ttl" );
        assertSame( TimeUnit.SECONDS, captchaUnitInMock, "Вызов Redis выполнен с другой размерностью ttl (unit)" );

        // Какие внешние методы вызывал
         Mockito.verify(template, Mockito.times(1)).opsForValue();
         // Вот тут заглушками мы просто определяем мнемонику метода,
         // проверки на вызов с определенным значением тут не будет
         // (она на самом деле сделана выше)
         Mockito.verify(valueOperations, Mockito.times(1))
                 .set(any(UUID.class), any(CaptchaInfo.class), anyLong(), any(TimeUnit.class));
    }


    @Test
    void updateWithNullTest() {
        assertThrows(NullPointerException.class, () -> repository.save(null),
                "При передаче null должен возникать NPE");
    }

    @Test
    void getWithNullTest() {
        assertThrows(NullPointerException.class, () -> repository.get(null),
                "При передаче null должен возникать NPE");
    }

    @Test
    void deleteWithNullTest() {
        assertThrows(NullPointerException.class, () -> repository.delete(null),
                "При передаче null должен возникать NPE");
    }

    @Test
    void getTest() {
        final CaptchaInfo info = new CaptchaInfo(
                new byte[] { 11, 12, 13},
                "testCode",
                new Date(123123123L),
                CaptchaInfo.Status.NEW);

        AtomicReference<UUID> uuidFromMock = new AtomicReference<>();

        when(valueOperations.get(any(UUID.class))).thenAnswer(invocation -> {
            uuidFromMock.set(invocation.getArgument(0, UUID.class));
            return info;
        });

        UUID uuid = UUID.randomUUID();

        CaptchaInfo info2 = repository.get(uuid);

        assertNotNull(info, "Ответ не должен быть null для известного объекта");
        assertSame(info, info2, "Вернулся не тот же объект, который был получен из redisTemplate");
        // Тут мы сами в рамках реализации придумали это правило,
        // на самом деле это часто не так (добавляют префикс вида использования)
        assertEquals(uuid, uuidFromMock.get(), "В redisTemplate должен передаваться тот же ключ, что и на входе");

        // Какие внешние методы вызывал
        Mockito.verify(template, Mockito.times(1)).opsForValue();
        Mockito.verify(valueOperations, Mockito.times(1)).get(any(UUID.class));
    }

    @Test
    void getWithNullResultTest() {

        UUID uuid = UUID.randomUUID();
        // Мы ничего не готовили специально -
        // вызов метода заглушки вернет null
        CaptchaInfo info = repository.get(uuid);

        assertNull(info, "Ответ в случае отсутствия данных должен быть null");

        // Какие внешние методы вызывал
        Mockito.verify(template, Mockito.times(1)).opsForValue();
        Mockito.verify(valueOperations, Mockito.times(1)).get(any(UUID.class));
    }

    @Test
    void delete() {

        AtomicReference<UUID> uuidFromMock = new AtomicReference<>();

        when(template.delete(any(UUID.class))).thenAnswer(invocation -> {
            uuidFromMock.set(invocation.getArgument(0, UUID.class));
            return true;
        });

        UUID uuid = UUID.randomUUID();
        repository.delete(uuid);

        // Тут мы сами в рамках реализации придумали это правило,
        // на самом деле это часто не так (добавляют префикс вида использования)
        assertEquals(uuid, uuidFromMock.get(), "В redisTemplate должен передаваться тот же ключ, что и на входе");

        // Какие внешние методы вызывал
        Mockito.verify(template, Mockito.times(1)).delete(any(UUID.class));

    }
}