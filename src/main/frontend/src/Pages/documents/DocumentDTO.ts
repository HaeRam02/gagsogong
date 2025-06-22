export interface DocumentDTO {
  docID: string;
  writerID: string;
  content: string;
  title: string;
  visibility: string;
  date: Date;
  attachments: AttachmentDTO[];
}

export interface AttachmentDTO {
  id: number;
  originalName: string;
  path: string;
  size: number;
}
