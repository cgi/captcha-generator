package io.github.cgi.captchagenerator.service;

import com.google.common.collect.Lists;
import com.wf.captcha.base.Captcha;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaServiceV2Test {

    private CaptchaServiceV2 serviceV2;


    public static class ServiceValidConfig implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            List<List<Integer>> variants = Lists.cartesianProduct(
                    List.of(4, 5, 6, 7, 8), // len
                    List.of(64, 74, 54, 40), // height
                    List.of(100, 110, 120, 113),  // width
                    List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) // Font
            );
            // Сколько у нас элементов?
            // System.out.println(variants.size());

            return variants.stream()
                    .map(List::toArray)
                    .map(Arguments::of);
        }
    }


    public static class ServiceInvalidConfig implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            List<List<Integer>> variants = Lists.cartesianProduct(
                    List.of(4, 5, 6, 7, 8), // len
                    List.of(64, 74, 54, 40), // height
                    List.of(100, 110, 120, 113),  // width
                    List.of(-1, 10, 11, 12, 13, 14, 15) // Font - некорректные номера
            );

            // Сколько у нас элементов?
            // System.out.println(variants.size());

            return variants.stream()
                    .map(List::toArray)
                    .map(Arguments::of);
        }
    }

    @BeforeEach
    void setUp() {
        serviceV2 = createCaptchaServiceV2(4, 64, 100, Captcha.FONT_3);
    }

    /**
     * Проверка работы самих тестов (начало рекурсии %) )
     */
    //@BeforeEach
    void mockSetUp() {
        CaptchaServiceV2 mock = Mockito.mock(CaptchaServiceV2.class);
        Mockito.when(mock.getCaptcha()).thenReturn( new CaptchaInfo() );
        serviceV2 = mock;
    }

    @Test
    void getCaptchaIsNotNull() {
        CaptchaInfo captchaInfo = serviceV2.getCaptcha();

        assertNotNull(captchaInfo,
                "Сервис вернул null для метода getCaptcha");
        assertNotNull(captchaInfo.getData(),
                "нет картинки");
        assertNotNull(captchaInfo.getCreated(),
                "нет времени создания");
        assertNotNull(captchaInfo.getStatus(),
                "нет статуса");
        assertNotNull(captchaInfo.getResultCode(),
                "нет правильного ответа");
    }

    @Test
    void getCaptchaStatusIsNEW(){
        CaptchaInfo captchaInfo = serviceV2.getCaptcha();

        assertEquals( CaptchaInfo.Status.NEW, captchaInfo.getStatus(),
                "Капча создалась не в статусе NEW");
    }

    /**
     * Проверяем параметры картинки для разных настроек сервиса.
     *
     * Примечание:
     * Если будем добавлять еще варианты:
     * 1. метод быстро раздуется и будет неудобным
     * 2. выполнение теста завершится на первой ошибке и её (ошибку)
     * нужно будет еще определить - по сообщению или стек-трейсу.
     *
     * См. вариант ниже для более удобного подхода.
     * @throws IOException
     */
    @Test
    void captchaImageSizeTest() throws IOException {
        CaptchaInfo captchaInfo = serviceV2.getCaptcha();
        byte[] data = captchaInfo.getData();
        BufferedImage image;
        image = getImage(data);

        assertEquals(64, image.getHeight(),
                "Картинка имеет неожиданную высоту" );
        assertEquals(100, image.getWidth() ,
                "Картинка имеет неожиданную ширину");

        CaptchaServiceV2 service = createCaptchaServiceV2(8, 50, 113, Captcha.FONT_4);
        captchaInfo = service.getCaptcha();

        image = getImage(captchaInfo.getData());

        assertEquals(50, image.getHeight(),
                "Картинка имеет неожиданную высоту" );
        assertEquals(113, image.getWidth() ,
                "Картинка имеет неожиданную ширину");
    }


    @ParameterizedTest
    @ArgumentsSource(ServiceValidConfig.class)
    void captchaImageSizeTestWithParams(int len, int height, int width, int font) throws IOException {

        CaptchaServiceV2 service = createCaptchaServiceV2(len, height, width, font);
        CaptchaInfo captchaInfo = service.getCaptcha();

        BufferedImage image = getImage(captchaInfo.getData());

        assertEquals(height, image.getHeight(),
                "Картинка имеет неожиданную высоту" );
        assertEquals(width, image.getWidth() ,
                "Картинка имеет неожиданную ширину");

        assertEquals( len, captchaInfo.getResultCode().length(),
                "Неожиданная длинна строки проверки");
    }

    @Test
    void captchaLengthTest(){
        CaptchaInfo captchaInfo = serviceV2.getCaptcha();

        assertEquals( 4, captchaInfo.getResultCode().length(),
                "Неожиданная длинна строки проверки");
    }


    @ParameterizedTest
    @ArgumentsSource(ServiceInvalidConfig.class)
    void captchaTestWithBadParams(int len, int height, int width, int font) throws IOException {

        CaptchaServiceV2 service = createCaptchaServiceV2(len, height, width, font);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            CaptchaInfo captchaInfo = service.getCaptcha();
        }, "Исключения не произошло");

    }

    private BufferedImage getImage(byte[] data) throws IOException {
        BufferedImage image;
        try(InputStream is = new ByteArrayInputStream(data)){
            image = ImageIO.read(is);
        }
        return image;
    }


    private CaptchaServiceV2 createCaptchaServiceV2(
            int captchaLen, int height, int width, int fontNum) {
        CaptchaServiceV2 s = new CaptchaServiceV2();
        s.setCaptchaLen(captchaLen);
        s.setHeight(height);
        s.setWidth(width);
        s.setFontNum(fontNum);
        return s;
    }
}