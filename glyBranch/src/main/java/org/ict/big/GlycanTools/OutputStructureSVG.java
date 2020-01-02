package org.ict.big.GlycanTools;

import org.apache.commons.io.FileUtils;
import org.eurocarbdb.application.glycanbuilder.BaseDocument;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.linkage.LinkageStyle;
import org.eurocarbdb.application.glycanbuilder.linkage.LinkageStyleDictionary;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.eurocarbdb.application.glycanbuilder.renderutil.SVGUtils;
import org.eurocarbdb.application.glycanbuilder.util.GraphicOptions;
import org.ict.big.GlycanTools.debug.Print;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

public class OutputStructureSVG {

    public static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace workspace = new BuilderWorkspace(glycanRenderer);
    private static GlycanDocument glycanDocument = null;


    public static void main(String[] args) {
        customizeWorkspace();
        loadStructures();
        customizeWorkspace();
        LinkedList<Glycan> allGlycans = glycanDocument.getStructures();
        int i = 0;
        for (Glycan g : allGlycans) {
            i++;
            LinkedList<Glycan> fragmentArray = new LinkedList<>();
            fragmentArray.add(g);
            String svgStr = SVGUtils.getVectorGraphics((GlycanRendererAWT) glycanRenderer, fragmentArray,
                    false, false);
            String filename = "RNaseB_" + String.valueOf(i) + ".svg";
            try {
                FileUtils.writeByteArrayToFile(new File("./structure-library/" + filename), svgStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Print.pl("test");
    }

    public static void customizeWorkspace() {
//        GraphicOptions options = new GraphicOptions();
        workspace.getGraphicOptions().NOTATION = GraphicOptions.NOTATION_SNFG;//GraphicOptions.NOTATION_CFG;
        glycanRenderer.setGraphicOptions(workspace.getGraphicOptions());
        workspace.getGraphicOptions().DISPLAY = GraphicOptions.DISPLAY_CUSTOM;//"custom";
        workspace.getGraphicOptions().MARGIN_TOP_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_BOTTOM_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_LEFT_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_RIGHT_CUSTOM = 10;
        try {
            // 使用反射配置 Linkage 显示信息
            LinkageStyleDictionary dictionary = glycanRenderer.getLinkageStyleDictionary();
            Field stylesField = LinkageStyleDictionary.class.getDeclaredField("styles");
            stylesField.setAccessible(true);
            LinkedList<LinkageStyle> linkageStyles = (LinkedList<LinkageStyle>) stylesField.get(dictionary);

            Field showInfoField = LinkageStyle.class.getDeclaredField("show_info");
            showInfoField.setAccessible(true);
            /**
             * Linkage 信息，一个三位长的字符串，第一位表示 [3, 6], 第二位表示 [α, β], 第三位表示左 linkage
             * 0 表示不绘制，其他字符表示绘制
             */
            for (LinkageStyle style : linkageStyles) {
                showInfoField.set(style, "000");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Print.pl("error in Main!!");
        }
    }

    public static void loadStructures(){
//        String path = "D:\\Work-temp\\Paper_scoring\\figure\\GWS\\RNaseB.gws";
        String path = "./structure-library/RNaseB.gws";
        GlycanDocument theStructures = getGlycanDocument();
        glycanDocument = theStructures;
        File file = new File(path);
        theStructures.open(file, true, false);
        Print.pl("test");
    }

    public static GlycanDocument getGlycanDocument(){
        Collection<BaseDocument> allDocuments = workspace.getAllDocuments();
        for(BaseDocument doc: allDocuments){
            if(doc instanceof GlycanDocument){
                return (GlycanDocument) doc;
            }
        }
        return null;
    }
}
