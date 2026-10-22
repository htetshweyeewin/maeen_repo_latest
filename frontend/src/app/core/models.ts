export interface Option {
  code: string;
  label: string;
}

export interface DesignationOption extends Option {
  levels: string[];
}

export interface Meta {
  bloodGroups: Option[];
  orgLevels: Option[];
  designations: DesignationOption[];
  orgRanks: Option[];
  statuses: Option[];
}

export interface GeoOption {
  id: number;
  name: string;
}

export interface Address {
  divisionId: number | null;
  divisionName?: string;
  districtId: number | null;
  districtName?: string;
  upazilaId: number | null;
  upazilaName?: string;
  unionId: number | null;
  unionName?: string;
  ward: number | null;
  postOffice: string | null;
  village: string | null;
}

export interface MemberRequest {
  name: string;
  fatherName: string;
  motherName: string;
  dateOfBirth: string;
  bloodGroup: string | null;
  education: string | null;
  email: string | null;
  mobile1: string;
  mobile1Call: boolean;
  mobile1WhatsApp: boolean;
  mobile2: string | null;
  mobile2Call: boolean;
  mobile2WhatsApp: boolean;
  permanentAddress: Address;
  presentSameAsPermanent: boolean;
  presentAddress: Address | null;
  responsibilityLevel: string | null;
  designation: string | null;
  orgRank: string;
  oathDate: string | null;
  /** শুধু কেন্দ্রীয় এডমিন: null = স্বয়ংক্রিয়, 0 = কোনো যেলা নয় */
  orgZilaId?: number | null;
}

export type MemberStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Member extends Omit<MemberRequest, 'orgZilaId'> {
  id: number;
  bloodGroupLabel: string | null;
  responsibilityLevelLabel: string | null;
  designationLabel: string | null;
  orgRankLabel: string;
  status: MemberStatus;
  statusLabel: string;
  rejectReason: string | null;
  orgZilaId: number | null;
  orgZilaName: string | null;
  orgZilaManual: boolean;
  reviewedBy: string | null;
  reviewedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Stats {
  total: number;
  pending: number;
  approved: number;
  rejected: number;
}

export interface MemberQuery {
  q?: string;
  status?: MemberStatus | '';
  orgZilaId?: number | null;
  orgRank?: string;
  level?: string;
  page?: number;
  size?: number;
}

export type Role = 'SUPER_ADMIN' | 'ZILA_ADMIN';

export interface UserInfo {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  orgZilaId: number | null;
  orgZilaName: string | null;
}

export interface LoginResponse {
  token: string;
  user: UserInfo;
}

export interface UpazilaRef {
  id: number;
  name: string;
  districtName: string;
}

export interface OrgZila {
  id: number;
  name: string;
  upazilas: UpazilaRef[];
  memberCount: number;
}

export interface AdminUser {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  roleLabel: string;
  orgZilaId: number | null;
  orgZilaName: string | null;
  active: boolean;
}

export interface AdminUserRequest {
  username: string;
  fullName: string;
  password: string | null;
  role: Role;
  orgZilaId: number | null;
  active: boolean;
}
