import { render, screen, waitFor, fireEvent, cleanup, within } from '@testing-library/svelte';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import  SearchPage  from '../routes/searches/+page.svelte';
import { page } from '$app/stores';
import { apiFetch } from '$lib/utils/fetch';
import { register, init, getLocaleFromNavigator, waitLocale } from 'svelte-i18n';

register('en', () => import('$lib/i18n/locales/en.json'));
// Add other locales as needed

init({
  fallbackLocale: 'en',
  initialLocale: 'en',
});

await waitLocale();

global.fetch = vi.fn();

describe('search page functions', () => {
    //Clear all mock ups and give the searchParameters
    beforeEach(()=> {
        cleanup();
        vi.clearAllMocks();
        vi.mock('$app/stores', () => ({
            page: {
                subscribe : (fn) => {
                    fn({
                        url : new URL('http://localhost:3000/searches?group=&subgroup='),
                    });
                    return () => {};
                },
            },
        }));
        
    });

    //Test of getGroupsSummary()
    it('should fetch group summary successfully and set groups', async() => {
        const mockGroups = [{name : 'Group1'}, {name: 'Group2'}];
        const mockResponse = {json : vi.fn().mockResolvedValue(mockGroups), ok:true};
        fetch.mockResolvedValue(mockResponse);
        render(SearchPage);
        await waitFor(() => expect(screen.getByText('Group1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('Group2')).toBeTruthy());
    });

    //Test of findSubGroups()
    it('should fetch subgroups and categories when a group is selected', async() => {
        const mockGroups = [{name : 'Group1'}];
        const mockSubGroups = [
            {
                id : 1,
                name : 'SubGroup3',
                groupId : 1,
                subGroupCharacteristics : [
                    { name: "Char1", orderPosition: 1 },
                    { name: "Char2", orderPosition: 2 },
                ],
                intrCount : 1,
                categoriesId : [1],
                pictureId : null
            }, 
            {
                id : 2,
                name : 'SubGroup4',
                groupId : 1,
                subGroupCharacteristics : [
                    { name: "Char3", orderPosition: 1 },
                    { name: "Char4", orderPosition: 2 },
                ],
                intrCount : 1,
                categoriesId : [2],
                pictureId : null
            }]; 
        const mockCategories = [
            { id : 1, groupName : 'Group1', subGroupName : 'SubGroup3', function : 'Fct1', name : 'Name1', shape : 'FN1', lenAbrv :'L1CM', pictureId : null}, 
            { id : 2, groupName : 'Group1', subGroupName : 'SubGroup4', function : 'Fct2', name : 'Name2', shape : 'FN2', lenAbrv :'L2CM', pictureId : null}
        ]
        const mockGroupsResponse = {json : vi.fn().mockResolvedValue(mockGroups), ok:true};
        const mockSubGroupsResponse = {json : vi.fn().mockResolvedValue(mockSubGroups), ok:true};
        const mockCategoriesResponse = {json : vi.fn().mockResolvedValue(mockCategories), ok:true};

        fetch.mockImplementation((url) => {
            if(url.includes("/api/subgroups/group/")){
                return Promise.resolve(mockSubGroupsResponse);
            }
            if(url.includes('/api/category/group/')){
                return Promise.resolve(mockCategoriesResponse);
            }
            if(url.includes("/api/groups/summary")){
                return Promise.resolve(mockGroupsResponse);
            }
            return Promise.reject(new Error('Wrong API call'));
        })

        render(SearchPage);
        fireEvent.change(screen.getByLabelText('Group'), { target : {value : 'Group1'}});
        
        
        await waitFor(() => expect(screen.getAllByText('Group1')).toBeTruthy());

        await waitFor(() => expect(screen.getAllByText('SubGroup3')).toBeTruthy());
        
        const fct1Elements = await screen.findAllByText('Fct1');
        expect(fct1Elements.length).toBeGreaterThan(0);

        await waitFor(() => expect(screen.getByText('Name1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('FN1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('L1CM')).toBeTruthy());

        await waitFor(() => expect(screen.getAllByText('SubGroup4')).toBeTruthy());
        const fct2Elements = await screen.findAllByText('Fct2');
        expect(fct2Elements.length).toBeGreaterThan(0);
        await waitFor(() => expect(screen.getByText('Name2')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('FN2')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('L2CM')).toBeTruthy());

    });

    //Test of findCharacteristics()
    it('should fetch categories when a subgroup is selected and display the characteristics', async() => {
        const mockGroups = [{name : 'Group1'}];
        const subGroups = ['SubGroup3'];
        const mockSubGroups = {
                id : 1,
                name : 'SubGroup3',
                groupId : 1,
                subGroupCharacteristics : [
                    { name: "Char1", orderPosition: 1 },
                    { name: "Char2", orderPosition: 2 },
                    { name: "Char3", orderPosition: null }
                ],
                intrCount : 1,
                categoriesId : [1],
                pictureId : null
        }; 
        const mockCategories = [
            { id : 1, groupName : 'Group1', subGroupName : 'SubGroup3', function : 'Fct1', name : 'Name1', shape : 'FN1', lenAbrv :'L1CM', pictureId : null}, 
        ]
        const mockGroupsResponse = {json : vi.fn().mockResolvedValue(mockGroups), ok:true};
        const mockSubGroupsResponse = {json : vi.fn().mockResolvedValue(mockSubGroups), ok:true};
        const mockCategoriesResponse = {json : vi.fn().mockResolvedValue(mockCategories), ok:true};

        fetch.mockImplementation((url) => {
            if(url.includes("/api/subgroups/")){
                return Promise.resolve(mockSubGroupsResponse);
            }
            if(url.includes('/api/category/subgroup/')){
                return Promise.resolve(mockCategoriesResponse);
            }
            if(url.includes("/api/groups/summary")){
                return Promise.resolve(mockGroupsResponse);
            }
            return Promise.reject(new Error('Wrong API call'));
        });

        render(SearchPage , {props: {subGroups , showSubGroups : true, showCategories : true }});

        fireEvent.change(screen.getByLabelText('Subgroup'), { target : {value : 'SubGroup3'}});

        await waitFor(() => expect(screen.getAllByText('Group1')).toBeTruthy());

        await waitFor(() => expect(screen.getAllByText('SubGroup3')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('Fct1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('Name1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('FN1')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('L1CM')).toBeTruthy());

        await waitFor(() => expect(screen.getByText('Char1:')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('Char2:')).toBeTruthy());
        await waitFor(() => expect(screen.getByText('Char3:')).toBeTruthy());

    });

    //No test of searchByCharacteristic because it is just displaying the categories which was verified above

    //Test of selectCategoryWithChar() and selectCategory() (since selectCategory() is called in selectCategoryWithChar())
    it('should fetch the instruments of the selected category and display the characteristic values of the category', async() => {
        const subGroups = ['SubGroup3'];
        const categories = [
            { id : 1, groupName : 'Group1', subGroupName : 'SubGroup3', function : 'Fct1', name : 'Name1', shape : 'FN1', lenAbrv :'L1CM', pictureId : null}, 
        ];
        const characteristics = ["Length","Char1", "Char2", "Char3"];
        
        const mockGroups = [{name : 'Group1'}];
        const mockInstruments = [
            {
                "supplier": "Supplier1",
                "categoryId": 1,
                "reference": "SP1-INSTR1",
                "supplierDescription": "Instrument1",
                "price": 10,
                "obsolete": false,
                "picturesId": null,
                "id": 1
             }
        ];
        const mockCategoryChars = [
            {"name" : "Length", "value" : "Value0", "abrev" : "10CM"},
            {"name" : "Char1", "value" : "Value1", "abrev" : "V1"},
            {"name" : "Char2", "value" : "Value2", "abrev" : "V2"},
            {"name" : "Char3", "value" : "Value3", "abrev" : "V3"}
        ]

        const mockGroupsResponse = {json: vi.fn().mockResolvedValue(mockGroups), ok:true};
        const mockInstrumentsResponse = {json: vi.fn().mockResolvedValue(mockInstruments), ok:true};
        const mockCategoryCharsResponse = {json: vi.fn().mockResolvedValue(mockCategoryChars), ok:true};

        fetch.mockImplementation((url) => {
            if(url.includes("/api/category/instruments/")){
                return Promise.resolve(mockInstrumentsResponse);
            }
            else if(url.includes("/api/category")){
                return Promise.resolve(mockCategoryCharsResponse);
            }
            else if(url.includes("/api/groups/summary")){
                return Promise.resolve(mockGroupsResponse);
            }
            return Promise.reject(new Error('Wrong API call'));
        });

        const { getByTestId } = render(SearchPage, {props: {subGroups , categories, characteristics, showSubGroups : true, showCategories : true, showChars : true }});

        const categoriesTable = getByTestId("categories-table");
        const rows = within(categoriesTable).getAllByRole('row');
        fireEvent.dblClick(rows[1]); //row 0 is the titles of the colomns

        //await waitFor(() => expect(screen.getByText('Supplier1')).toBeTruthy());
        //await waitFor(() => expect(screen.getAllByText('SP1-INSTR1')).toBeTruthy());
        //await waitFor(() => expect(screen.getByText('Instrument1')).toBeTruthy());
        //await waitFor(() => expect(screen.getByText('10')).toBeTruthy());
        //await waitFor(() => expect(screen.getByTestId('Char1').value).toBe("Value1"));
        //await waitFor(() => expect(screen.getByTestId('Char2').value).toBe("Value2"));
        //await waitFor(() => expect(screen.getByTestId('Char3').value).toBe("Value3"));
    });

});
