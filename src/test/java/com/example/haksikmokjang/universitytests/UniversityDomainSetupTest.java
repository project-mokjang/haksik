package com.example.haksikmokjang.universitytests;

import com.example.haksikmokjang.domain.school.UniversityDomain;
import com.example.haksikmokjang.repository.UniversityDomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class UniversityDomainSetupTest {

    @Autowired
    private UniversityDomainRepository universityDomainRepository;

    @Test
    @Commit
    @DisplayName("팀원 로컬 DB 동기화용: 전국 대학교 도메인 150+개 주입")
    void insertUniversityDomains() {
        // 1. 이미 데이터가 있으면 중복 삽입 방지 (근손실 방지)
        if (universityDomainRepository.count() > 0) {
            System.out.println("✅ 이미 대학교 도메인 데이터가 존재합니다. 주입을 생략합니다.");
            return;
        }

        // 2. 150개의 데이터를 2차원 배열로 압축 세팅 (하드코딩 방지)
        String[][] domainData = {
                {"아주대학교", "ajou.ac.kr"}, {"국립안동대학교", "andong.ac.kr"}, {"안양대학교", "anyang.ac.kr"},
                {"아신대학교", "acts.ac.kr"}, {"백석대학교", "bu.ac.kr"}, {"부산교육대학교", "bnue.ac.kr"},
                {"부산장신대학교", "bpu.ac.kr"}, {"부산외국어대학교", "bufs.ac.kr"}, {"가톨릭관동대학교", "cku.ac.kr"},
                {"가톨릭대학교", "cuk.ac.kr"}, {"부산가톨릭대학교", "cup.ac.kr"}, {"대구가톨릭대학교", "cataegu.ac.kr"},
                {"차의과학대학교", "cha.ac.kr"}, {"차의과학대학교", "chamc.co.kr"}, {"창신대학교", "cs.ac.kr"},
                {"국립창원대학교", "changwon.ac.kr"}, {"청주교육대학교", "cje.ac.kr"}, {"청주교육대학교", "chongju-e.ac.kr"},
                {"청주대학교", "cju.ac.kr"}, {"청주대학교", "chongju.ac.kr"}, {"초당대학교", "chodang.ac.kr"},
                {"초당대학교", "cdu.ac.kr"}, {"전남대학교", "chonnam.ac.kr"}, {"전남대학교", "jnu.ac.kr"},
                {"총신대학교", "chongshin.ac.kr"}, {"조선대학교", "chosun.ac.kr"}, {"추계예술대학교", "chugye.ac.kr"},
                {"춘천교육대학교", "cnue.ac.kr"}, {"춘천교육대학교", "cnue-e.ac.kr"}, {"중앙대학교", "cau.ac.kr"},
                {"충북대학교", "chungbuk.ac.kr"}, {"충북대학교", "cbnu.ac.kr"}, {"충남대학교", "chungnam.ac.kr"},
                {"충남대학교", "cnu.ac.kr"}, {"청운대학교", "chungwoon.ac.kr"}, {"대구예술대학교", "tau.ac.kr"},
                {"대구경북과학기술원", "dgist.ac.kr"}, {"대구한의대학교", "dhu.ac.kr"}, {"대구교육대학교", "dnue.ac.kr"},
                {"대구대학교", "daegu.ac.kr"}, {"대구대학교", "taegu.ac.kr"}, {"대구외국어대학교", "dufs.ac.kr"},
                {"대전가톨릭대학교", "dcatholic.ac.kr"}, {"대전신학대학교", "daejeon.ac.kr"}, {"대전대학교", "dju.ac.kr"},
                {"대전대학교", "taejon.ac.kr"}, {"대진대학교", "daejin.ac.kr"}, {"대신대학교", "daeshin.ac.kr"},
                {"단국대학교", "dankook.ac.kr"}, {"동아대학교", "donga.ac.kr"}, {"동의대학교", "dongeui.ac.kr"},
                {"동의대학교", "deu.ac.kr"}, {"동덕여자대학교", "dongduk.ac.kr"}, {"동국대학교", "dongguk.ac.kr"},
                {"동국대학교", "dongguk.edu"}, {"동서대학교", "dongseo.ac.kr"}, {"동신대학교", "dongshinu.ac.kr"},
                {"동신대학교", "dsu.ac.kr"}, {"동양대학교", "dyu.ac.kr"}, {"동양대학교", "dytc.ac.kr"},
                {"덕성여자대학교", "duksung.ac.kr"}, {"을지대학교", "eulji.ac.kr"}, {"이화여자대학교", "ewha.ac.kr"},
                {"극동대학교", "kdu.ac.kr"}, {"가천대학교", "gachon.ac.kr"}, {"가천대학교", "kyungwon.ac.kr"},
                {"국립강릉원주대학교", "gwnu.ac.kr"}, {"국립강릉원주대학교", "kangnung.ac.kr"}, {"금강대학교", "ggu.ac.kr"},
                {"김천대학교", "gimcheon.ac.kr"}, {"공주교육대학교", "gjue.ac.kr"}, {"공주교육대학교", "kongju-e.ac.kr"},
                {"광주가톨릭대학교", "gjcatholic.ac.kr"}, {"광주과학기술원", "gist.ac.kr"}, {"광주교육대학교", "gnue.ac.kr"},
                {"광주교육대학교", "kwangju-e.ac.kr"}, {"경인교육대학교", "ginue.ac.kr"}, {"경인교육대학교", "inchon-e.ac.kr"},
                {"신경주대학교", "kyongju.ac.kr"}, {"경상국립대학교", "gnu.ac.kr"}, {"경상국립대학교", "gsnu.ac.kr"},
                {"경상국립대학교", "gntech.ac.kr"}, {"경상국립대학교", "chinju.ac.kr"}, {"한라대학교", "halla.ac.kr"},
                {"한림대학교", "hallym.ac.kr"}, {"국립한밭대학교", "hanbat.ac.kr"}, {"국립한밭대학교", "tnut.ac.kr"},
                {"한동대학교", "handong.edu"}, {"한일장신대학교", "hanil.ac.kr"}, {"한경국립대학교", "hknu.ac.kr"},
                {"한국외국어대학교", "hufs.ac.kr"}, {"한려대학교", "hanlyo.ac.kr"}, {"한남대학교", "hannam.ac.kr"},
                {"한세대학교", "hansei.ac.kr"}, {"한서대학교", "hanseo.ac.kr"}, {"한신대학교", "hanshin.ac.kr"},
                {"한신대학교", "hs.ac.kr"}, {"한성대학교", "hansung.ac.kr"}, {"한양대학교", "hanyang.ac.kr"},
                {"한중대학교", "hanzhong.ac.kr"}, {"호남신학대학교", "htus.ac.kr"}, {"호남대학교", "honam.ac.kr"},
                {"홍익대학교", "hongik.ac.kr"}, {"호서대학교", "hoseo.ac.kr"}, {"호원대학교", "howon.ac.kr"},
                {"협성대학교", "hyupsung.ac.kr"}, {"협성대학교", "uhs.ac.kr"}, {"인천가톨릭대학교", "iccu.ac.kr"},
                {"인천대학교", "inu.ac.kr"}, {"인천대학교", "inchon.ac.kr"}, {"한국정보통신대학교", "icu.ac.kr"},
                {"인하대학교", "inha.ac.kr"}, {"인제대학교", "inje.ac.kr"}, {"제주국제대학교", "jeju.ac.kr"},
                {"제주대학교", "jejunu.ac.kr"}, {"제주대학교", "cheju.ac.kr"}, {"제주대학교", "cheju-e.ac.kr"},
                {"전북대학교", "jbnu.ac.kr"}, {"전북대학교", "jeonbuk.ac.kr"}, {"전북대학교", "chonbuk.ac.kr"},
                {"전주교육대학교", "jnue.ac.kr"}, {"전주교육대학교", "chonju-e.ac.kr"}, {"전주대학교", "jeonju.ac.kr"},
                {"전주대학교", "jj.ac.kr"}, {"예수대학교", "jesus.ac.kr"}, {"중앙승가대학교", "sangha.ac.kr"},
                {"중부대학교", "joongbu.ac.kr"}, {"중원대학교", "jwu.ac.kr"}, {"강남대학교", "kangnam.ac.kr"},
                {"강원대학교", "kangwon.ac.kr"}, {"강원대학교", "samchok.ac.kr"}, {"가야대학교", "kaya.ac.kr"},
                {"강서대학교", "kcu.ac.kr"}, {"KDI국제정책대학원", "kdischool.ac.kr"}, {"계명대학교", "keimyung.ac.kr"},
                {"계명대학교", "kmu.ac.kr"}, {"한국에너지공과대학교", "kentech.ac.kr"}, {"꽃동네대학교", "kkot.ac.kr"},
                {"국립공주대학교", "kongju.ac.kr"}, {"건국대학교", "konkuk.ac.kr"}, {"건양대학교", "konyang.ac.kr"},
                {"국민대학교", "kookmin.ac.kr"}, {"한국과학기술원", "kaist.ac.kr"}, {"한국과학기술원", "kaist.edu"},
                {"한국항공대학교", "kau.ac.kr"}, {"한국항공대학교", "hangkong.ac.kr"}, {"국군간호사관학교", "kafna.ac.kr"},
                {"한국침례신학대학교", "kbtus.ac.kr"}, {"국립한국해양대학교", "kmou.ac.kr"}, {"국립한국해양대학교", "kmaritime.ac.kr"},
                {"육군사관학교", "kma.ac.kr"}, {"한국방송통신대학교", "knou.ac.kr"}, {"한국체육대학교", "knsu.ac.kr"},
                {"한국체육대학교", "knupe.ac.kr"}, {"한국예술종합학교", "knua.ac.kr"}, {"한국예술종합학교", "karts.ac.kr"},
                {"한국전통문화대학교", "nuch.ac.kr"}, {"한국교원대학교", "knue.ac.kr"}, {"국립한국교통대학교", "ut.ac.kr"},
                {"국립한국교통대학교", "chungju.ac.kr"}, {"나사렛대학교", "kornu.ac.kr"}, {"고려대학교", "korea.ac.kr"},
                {"한국기술교육대학교", "koreatech.ac.kr"}, {"한국성서대학교", "bible.ac.kr"}, {"경찰대학", "police.ac.kr"},
                {"고신대학교", "kosin.ac.kr"}, {"국립금오공과대학교", "kumoh.ac.kr"}, {"국립군산대학교", "kunsan.ac.kr"},
                {"광주대학교", "kwangju.ac.kr"}, {"광주대학교", "gwangju.ac.kr"}, {"광주여자대학교", "kwu.ac.kr"},
                {"광신대학교", "kwangshin.ac.kr"}, {"광운대학교", "kwangwoon.ac.kr"}, {"광운대학교", "kw.ac.kr"},
                {"경기대학교", "kyonggi.ac.kr"}, {"경희대학교", "kyunghee.ac.kr"}, {"경희대학교", "khu.ac.kr"},
                {"경동대학교", "kduniv.ac.kr"}, {"경일대학교", "kyungil.ac.kr"}, {"경일대학교", "kiu.ac.kr"},
                {"경남대학교", "kyungnam.ac.kr"}, {"경북대학교", "kyungpook.ac.kr"}, {"경성대학교", "kyungsung.ac.kr"},
                {"경성대학교", "ks.ac.kr"}, {"경운대학교", "ikw.ac.kr"}, {"감리교신학대학교", "mtu.ac.kr"},
                {"목포가톨릭대학교", "mcu.ac.kr"}, {"국립목포해양대학교", "mmu.ac.kr"}, {"국립목포대학교", "mokpo.ac.kr"},
                {"목원대학교", "mokwon.ac.kr"}, {"명지대학교", "myongji.ac.kr"}, {"명지대학교", "mju.ac.kr"},
                {"남부대학교", "nambu.ac.kr"}, {"남서울대학교", "nsu.ac.kr"}, {"배재대학교", "pcu.ac.kr"},
                {"배재대학교", "paichai.ac.kr"}, {"포항공과대학교", "postech.ac.kr"}, {"포항공과대학교", "postech.edu"},
                {"장로회신학대학교", "puts.ac.kr"}, {"국립부경대학교", "pknu.ac.kr"}, {"국립부경대학교", "pukyong.ac.kr"},
                {"부산대학교", "pusan.ac.kr"}, {"부산대학교", "miryang.ac.kr"}, {"부산교육대학교", "pusan-e.ac.kr"},
                {"부산여자대학교", "pwc.ac.kr"}, {"평택대학교", "ptu.ac.kr"}, {"평택대학교", "ptuniv.ac.kr"},
                {"공군사관학교", "afa.ac.kr"}, {"해군사관학교", "navy.ac.kr"}, {"삼육대학교", "syu.ac.kr"},
                {"상지대학교", "sangji.ac.kr"}, {"상명대학교", "sangmyung.ac.kr"}, {"상명대학교", "smu.ac.kr"},
                {"세한대학교", "sehan.ac.kr"}, {"세한대학교", "daebul.ac.kr"}, {"세종대학교", "sejong.ac.kr"},
                {"세명대학교", "semyung.ac.kr"}, {"서경대학교", "skuniv.ac.kr"}, {"서경대학교", "seokyeong.ac.kr"},
                {"서남대학교", "seonam.ac.kr"}, {"서울기독대학교", "scu.ac.kr"}, {"서울한영대학교", "hytu.ac.kr"},
                {"서울장신대학교", "sjs.ac.kr"}, {"서울대학교", "snu.ac.kr"}, {"서울교육대학교", "snue.ac.kr"},
                {"서울교육대학교", "seoul-e.ac.kr"}, {"서울과학기술대학교", "seoultech.ac.kr"}, {"서울과학기술대학교", "snut.ac.kr"},
                {"서울신학대학교", "stu.ac.kr"}, {"서울여자대학교", "swu.ac.kr"}, {"서원대학교", "seowon.ac.kr"},
                {"화성의과학대학교", "sgu.ac.kr"}, {"신한대학교", "shinhan.ac.kr"}, {"신라대학교", "silla.ac.kr"},
                {"서강대학교", "sogang.ac.kr"}, {"송원대학교", "songwon.ac.kr"}, {"숙명여자대학교", "sookmyung.ac.kr"},
                {"순천향대학교", "sch.ac.kr"}, {"숭실대학교", "soongsil.ac.kr"}, {"숭실대학교", "ssu.ac.kr"},
                {"국립순천대학교", "sunchon.ac.kr"}, {"국립순천대학교", "scnu.ac.kr"}, {"성공회대학교", "skhu.ac.kr"},
                {"성결대학교", "sungkyul.ac.kr"}, {"성균관대학교", "skku.edu"}, {"성균관대학교", "skku.ac.kr"},
                {"성신여자대학교", "sungshin.ac.kr"}, {"선문대학교", "sunmoon.ac.kr"}, {"수원가톨릭대학교", "suwoncatholic.ac.kr"},
                {"수원대학교", "suwon.ac.kr"}, {"태재대학교", "taejae.ac.kr"}, {"한국공학대학교", "tukorea.ac.kr"},
                {"한국공학대학교", "kpu.ac.kr"}, {"동명대학교", "tu.ac.kr"}, {"동명대학교", "tit.ac.kr"},
                {"유원대학교", "u1.ac.kr"}, {"위덕대학교", "uu.ac.kr"}, {"울산과학기술원", "unist.ac.kr"},
                {"과학기술연합대학원대학교", "ust.ac.kr"}, {"서울시립대학교", "uos.ac.kr"}, {"울산대학교", "ulsan.ac.kr"},
                {"원광대학교", "wonkwang.ac.kr"}, {"원광대학교", "wku.ac.kr"}, {"우송대학교", "wsu.ac.kr"},
                {"우석대학교", "woosuk.ac.kr"}, {"영남대학교", "yeungnam.ac.kr"}, {"예원예술대학교", "yewon.ac.kr"},
                {"용인대학교", "yongin.ac.kr"}, {"연세대학교", "yonsei.ac.kr"}, {"여수대학교", "yosu.ac.kr"},
                {"영산대학교", "ysu.ac.kr"}, {"영산대학교", "youngsan.ac.kr"}
        };

        // 3. 배열을 순회하며 엔티티로 조립
        List<UniversityDomain> domains = new ArrayList<>();
        for (String[] data : domainData) {
            domains.add(UniversityDomain.builder()
                    .universityName(data[0])
                    .emailDomain(data[1])
                    .build());
        }

        // 4. DB 창고에 한방에 Insert (벌크 인서트)
        universityDomainRepository.saveAll(domains);

        System.out.println("🚀 완벽하게 " + domains.size() + "개의 대학 도메인 데이터가 로컬 DB에 꽂혔습니다!");
    }
}