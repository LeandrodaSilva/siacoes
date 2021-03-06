package br.edu.utfpr.dv.siacoes.window;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

import br.edu.utfpr.dv.siacoes.model.JuryAppraiser;
import br.edu.utfpr.dv.siacoes.model.SigetConfig;
import br.edu.utfpr.dv.siacoes.Session;
import br.edu.utfpr.dv.siacoes.bo.JuryAppraiserBO;
import br.edu.utfpr.dv.siacoes.bo.SigetConfigBO;
import br.edu.utfpr.dv.siacoes.components.FileUploader;
import br.edu.utfpr.dv.siacoes.components.FileUploaderListener;
import br.edu.utfpr.dv.siacoes.model.Document.DocumentType;
import br.edu.utfpr.dv.siacoes.util.ExtensionUtils;

public class EditJuryAppraiserFeedbackWindow extends EditWindow {
	
	private final JuryAppraiser appraiser;
	
	private final FileUploader uploadFile;
	private final FileUploader uploadAdditionalFile;
	private final Button buttonDownload;
	private final Button buttonDownloadAdditional;
	
	private Button.ClickListener listenerClickDownloadAdditional;
	
	private SigetConfig config;

	public EditJuryAppraiserFeedbackWindow(JuryAppraiser appraiser){
		super("Enviar Feedback", null);
		
		if(appraiser == null){
			this.appraiser = new JuryAppraiser();
		}else{
			this.appraiser = appraiser;
		}
		
		try {
			this.config = new SigetConfigBO().findByDepartment(new JuryAppraiserBO().findIdDepartment(this.appraiser.getIdJuryAppraiser()));
		} catch (Exception e) {
			this.config = new SigetConfig();
		}
		
		this.uploadFile = new FileUploader("Arquivo Comentado (Formato PDF, " + this.config.getMaxFileSizeAsString() + ")");
		this.uploadFile.getAcceptedDocumentTypes().add(DocumentType.PDF);
		this.uploadFile.setMaxBytesLength(this.config.getMaxFileSize());
		this.uploadFile.setFileUploadListener(new FileUploaderListener() {
			@Override
			public void uploadSucceeded() {
				if(uploadFile.getUploadedFile() != null) {
					appraiser.setFile(uploadFile.getUploadedFile());
					
					buttonDownload.setVisible(true);
				}
			}
		});
		
		this.uploadAdditionalFile = new FileUploader("Arquivos Complementares (Formato ZIP, " + this.config.getMaxFileSizeAsString() + ")");
		this.uploadAdditionalFile.getAcceptedDocumentTypes().add(DocumentType.ZIP);
		this.uploadAdditionalFile.setMaxBytesLength(this.config.getMaxFileSize());
		this.uploadAdditionalFile.setFileUploadListener(new FileUploaderListener() {
			@Override
			public void uploadSucceeded() {
				if(uploadAdditionalFile.getUploadedFile() != null) {
					appraiser.setAdditionalFile(uploadAdditionalFile.getUploadedFile());
					
					prepareDownloadAdditionalFeedback();
				}
			}
		});
		
		this.buttonDownload = new Button("Download", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	downloadFeedback();
            }
        });
		this.buttonDownload.setIcon(FontAwesome.DOWNLOAD);
		this.buttonDownload.setWidth("100px");
		this.buttonDownload.setVisible(this.appraiser.getFile() != null);
		
		this.buttonDownloadAdditional = new Button("Download");
		this.buttonDownloadAdditional.setIcon(FontAwesome.DOWNLOAD);
		this.buttonDownloadAdditional.setWidth("100px");
		this.buttonDownloadAdditional.setVisible(this.appraiser.getAdditionalFile() != null);
		
		this.addField(new HorizontalLayout(this.uploadFile, this.buttonDownload));
		this.addField(new HorizontalLayout(this.uploadAdditionalFile, this.buttonDownloadAdditional));
	}
	
	@Override
	public void save() {
		if(this.uploadFile.getUploadedFile() != null) {
			this.appraiser.setFile(this.uploadFile.getUploadedFile());
		}
		if(this.uploadAdditionalFile.getUploadedFile() != null) {
			this.appraiser.setAdditionalFile(this.uploadAdditionalFile.getUploadedFile());
		}
		
		if((this.appraiser.getFile() == null) && (this.appraiser.getAdditionalFile() == null)){
			this.showErrorNotification("Enviar Feedback", "É necessário submeter ao menos um arquivo.");
		}else{
			try{
				JuryAppraiserBO bo = new JuryAppraiserBO();
				
				bo.save(Session.getIdUserLog(), this.appraiser);
				
				this.showSuccessNotification("Enviar Feedback", "Feedback enviado com sucesso.");
				
				this.parentViewRefreshGrid();
				this.close();
			}catch(Exception e){
				Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
				
				this.showErrorNotification("Enviar Feedback", e.getMessage());
			}
		}
	}
	
	private void downloadFeedback() {
		if(this.appraiser.getFile() != null) {
			this.showReport(this.appraiser.getFile());
		} else {
			this.showWarningNotification("Download do Arquivo", "Nenhum arquivo foi enviado.");
		}
	}
	
	private void prepareDownloadAdditionalFeedback() {
		this.buttonDownloadAdditional.removeClickListener(this.listenerClickDownloadAdditional);
		new ExtensionUtils().removeAllExtensions(this.buttonDownloadAdditional);
		
    	if(this.appraiser.getAdditionalFile() != null) {
    		this.buttonDownloadAdditional.setVisible(true);
    		new ExtensionUtils().extendToDownload(this.appraiser.getAppraiser().getName() + ".zip", this.appraiser.getAdditionalFile(), this.buttonDownloadAdditional);
    	} else {
    		this.buttonDownloadAdditional.setVisible(false);
    		this.listenerClickDownloadAdditional = new Button.ClickListener() {
	            @Override
	            public void buttonClick(ClickEvent event) {
	            	showWarningNotification("Download de Arquivo", "Nenhum arquivo foi enviado.");
	            }
	        };
	        
    		this.buttonDownloadAdditional.addClickListener(this.listenerClickDownloadAdditional);
    	}
	}
	
}
